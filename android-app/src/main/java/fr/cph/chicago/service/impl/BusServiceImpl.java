package fr.cph.chicago.service.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fr.cph.chicago.R;
import fr.cph.chicago.client.CtaClient;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.util.Util;
import io.reactivex.exceptions.Exceptions;

import static fr.cph.chicago.client.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.client.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.client.CtaRequestType.BUS_PATTERN;
import static fr.cph.chicago.client.CtaRequestType.BUS_ROUTES;
import static fr.cph.chicago.client.CtaRequestType.BUS_STOP_LIST;
import static fr.cph.chicago.client.CtaRequestType.BUS_VEHICLES;

public enum BusServiceImpl implements BusService {
    INSTANCE;

    @NonNull
    @Override
    public List<BusArrival> loadFavoritesBuses(@NonNull final Context context) {
        final Set<BusArrival> busArrivals = new LinkedHashSet<>();
        final MultiValuedMap<String, String> paramBus = Util.getFavoritesBusParams(context);
        // Load bus
        try {
            final List<String> rts = new ArrayList<>();
            final List<String> stpids = new ArrayList<>();
            for (final Map.Entry<String, Collection<String>> entry : paramBus.asMap().entrySet()) {
                final String key = entry.getKey();
                StringBuilder str = new StringBuilder();
                int i = 0;
                final List<String> values = (List<String>) entry.getValue();
                for (final String v : values) {
                    str.append(v).append(",");
                    if (i == 9 || i == values.size() - 1) {
                        if ("rt".equals(key)) {
                            rts.add(str.toString());
                        } else if ("stpid".equals(key)) {
                            stpids.add(str.toString());
                        }
                        str = new StringBuilder();
                        i = -1;
                    }
                    i++;
                }
            }
            for (int i = 0; i < rts.size(); i++) {
                final MultiValuedMap<String, String> para = new ArrayListValuedHashMap<>();
                para.put(context.getString(R.string.request_rt), rts.get(i));
                para.put(context.getString(R.string.request_stop_id), stpids.get(i));
                final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(BUS_ARRIVALS, para);
                busArrivals.addAll(XmlParser.Companion.getINSTANCE().parseBusArrivals(xmlResult));
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
        return Stream.of(busArrivals).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public List<BusStop> loadOneBusStop(@NonNull final Context context, @NonNull final String stopId, @NonNull final String bound) {
        try {
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            params.put(context.getString(R.string.request_rt), stopId);
            params.put(context.getString(R.string.request_dir), bound);
            final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(BUS_STOP_LIST, params);
            return XmlParser.Companion.getINSTANCE().parseBusBounds(xmlResult);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public BusData loadLocalBusData(@NonNull final Context context) {
        BusData.INSTANCE.readBusStopsIfNeeded(context);
        return BusData.INSTANCE;
    }

    @NonNull
    @Override
    public BusDirections loadBusDirections(@NonNull final Context context, @NonNull final String busRouteId) {
        try {
            final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
            reqParams.put(context.getString(R.string.request_rt), busRouteId);
            final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(BUS_DIRECTION, reqParams);
            return XmlParser.Companion.getINSTANCE().parseBusDirections(xmlResult, busRouteId);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public List<BusRoute> loadBusRoutes() {
        try {
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(BUS_ROUTES, params);
            return XmlParser.Companion.getINSTANCE().parseBusRoutes(xmlResult);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public List<BusArrival> loadFollowBus(@NonNull final Context context, @NonNull final String busId) {
        try {
            final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
            connectParam.put(context.getString(R.string.request_vid), busId);
            final InputStream content = CtaClient.Companion.getINSTANCE().connect(BUS_ARRIVALS, connectParam);
            return XmlParser.Companion.getINSTANCE().parseBusArrivals(content);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public Optional<BusPattern> loadBusPattern(@NonNull final Context context, @NonNull final String busRouteId, @NonNull final String bound) {
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        connectParam.put(context.getString(R.string.request_rt), busRouteId);
        final String boundIgnoreCase = bound.toLowerCase(Locale.US);
        try {
            final InputStream content = CtaClient.Companion.getINSTANCE().connect(BUS_PATTERN, connectParam);
            final List<BusPattern> patterns = XmlParser.Companion.getINSTANCE().parsePatterns(content);
            return Stream.of(patterns)
                .filter(pattern -> {
                    final String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
                    return pattern.getDirection().equals(bound) || boundIgnoreCase.contains(directionIgnoreCase);
                })
                .findFirst()
                .or(Optional::empty);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public List<Bus> loadBus(@NonNull final Context context, final int busId, @NonNull final String busRouteId) {
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        if (busId != 0) {
            connectParam.put(context.getString(R.string.request_vid), Integer.toString(busId));
        } else {
            connectParam.put(context.getString(R.string.request_rt), busRouteId);
        }
        try {
            final InputStream content = CtaClient.Companion.getINSTANCE().connect(BUS_VEHICLES, connectParam);
            return XmlParser.Companion.getINSTANCE().parseVehicles(content);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @Override
    public List<BusArrival> loadAroundBusArrivals(@NonNull final Context context, @NonNull final BusStop busStop) {
        try {
            int busStopId = busStop.getId();
            final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>(1, 1);
            reqParams.put(context.getString(R.string.request_stop_id), Integer.toString(busStopId));
            final InputStream is = CtaClient.Companion.getINSTANCE().connect(BUS_ARRIVALS, reqParams);
            return XmlParser.Companion.getINSTANCE().parseBusArrivals(is);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}
