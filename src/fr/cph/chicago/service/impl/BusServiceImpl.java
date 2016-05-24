package fr.cph.chicago.service.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;
import rx.exceptions.Exceptions;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.connection.CtaRequestType.BUS_PATTERN;
import static fr.cph.chicago.connection.CtaRequestType.BUS_ROUTES;
import static fr.cph.chicago.connection.CtaRequestType.BUS_STOP_LIST;
import static fr.cph.chicago.connection.CtaRequestType.BUS_VEHICLES;

public class BusServiceImpl implements BusService {

    private final XmlParser xmlParser;

    public BusServiceImpl() {
        this.xmlParser = XmlParser.getInstance();
    }

    @NonNull
    @Override
    public List<BusArrival> loadFavoritesBuses() {
        final List<BusArrival> busArrivals = new ArrayList<>();
        final MultiValuedMap<String, String> paramBus = Util.getFavoritesBusParams();
        // Load bus
        try {
            final CtaConnect ctaConnect = CtaConnect.getInstance();
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
                para.put(App.getContext().getString(R.string.request_rt), rts.get(i));
                para.put(App.getContext().getString(R.string.request_stop_id), stpids.get(i));
                final InputStream xmlResult = ctaConnect.connect(BUS_ARRIVALS, para);
                busArrivals.addAll(xmlParser.parseBusArrivals(xmlResult));
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
        return busArrivals;
    }

    @NonNull
    @Override
    public List<BusStop> loadOneBusStop(@NonNull final String stopId, @NonNull final String bound) {
        try {
            final CtaConnect connect = CtaConnect.getInstance();
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            params.put(App.getContext().getString(R.string.request_rt), stopId);
            params.put(App.getContext().getString(R.string.request_dir), bound);
            final InputStream xmlResult = connect.connect(BUS_STOP_LIST, params);
            final XmlParser xml = XmlParser.getInstance();
            return xml.parseBusBounds(xmlResult);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public BusData loadLocalBusData() {
        BusData.getInstance().readBusStops();
        return BusData.getInstance();
    }

    @NonNull
    @Override
    public BusDirections loadBusDirections(@NonNull final String busRouteId) {
        try {
            final CtaConnect connect = CtaConnect.getInstance();
            final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
            reqParams.put(App.getContext().getString(R.string.request_rt), busRouteId);
            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = connect.connect(BUS_DIRECTION, reqParams);
            return xml.parseBusDirections(xmlResult, busRouteId);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public List<BusRoute> loadBusRoutes() {
        try {
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            final CtaConnect connect = CtaConnect.getInstance();
            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = connect.connect(BUS_ROUTES, params);
            return xml.parseBusRoutes(xmlResult);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @NonNull
    @Override
    public List<BusArrival> loadFollowBus(@NonNull final String busId) {
        try {
            CtaConnect connect = CtaConnect.getInstance();
            MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
            connectParam.put(App.getContext().getString(R.string.request_vid), busId);
            InputStream content = connect.connect(BUS_ARRIVALS, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            return xml.parseBusArrivals(content);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @Nullable
    @Override
    public BusPattern loadBusPattern(@NonNull final String busRouteId, @NonNull final String bound) {
        final CtaConnect connect = CtaConnect.getInstance();
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        connectParam.put(App.getContext().getString(R.string.request_rt), busRouteId);
        final String boundIgnoreCase = bound.toLowerCase(Locale.US);
        try {
            final InputStream content = connect.connect(BUS_PATTERN, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            final List<BusPattern> patterns = xml.parsePatterns(content);
            for (final BusPattern pattern : patterns) {
                final String directionIgnoreCase = pattern.getDirection().toLowerCase(Locale.US);
                if (pattern.getDirection().equals(bound) || boundIgnoreCase.contains(directionIgnoreCase)) {
                    return pattern;
                }
            }
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
        return null;
    }

    @NonNull
    @Override
    public List<Bus> loadBus(final int busId, @NonNull final String busRouteId) {
        final CtaConnect connect = CtaConnect.getInstance();
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        if (busId != 0) {
            connectParam.put(App.getContext().getString(R.string.request_vid), Integer.toString(busId));
        } else {
            connectParam.put(App.getContext().getString(R.string.request_rt), busRouteId);
        }
        try {
            final InputStream content = connect.connect(BUS_VEHICLES, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            return xml.parseVehicles(content);
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}