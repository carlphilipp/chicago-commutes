package fr.cph.chicago.service;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;
import rx.exceptions.Exceptions;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.BUS_DIRECTION;
import static fr.cph.chicago.connection.CtaRequestType.BUS_STOP_LIST;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

public class DataServiceImpl implements DataService {

    private final JsonParser jsonParser;
    private final XmlParser xmlParser;

    public DataServiceImpl() {
        this.jsonParser = JsonParser.getInstance();
        this.xmlParser = XmlParser.getInstance();
    }

    @Override
    public SparseArray<TrainArrival> loadFavoritesTrain() {
        final MultiValuedMap<String, String> trainParams = Util.getFavoritesTrainParams();
        SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        try {
            final CtaConnect ctaConnect = CtaConnect.getInstance();
            for (final Map.Entry<String, Collection<String>> entry : trainParams.asMap().entrySet()) {
                final String key = entry.getKey();
                if ("mapid".equals(key)) {
                    final List<String> list = (List<String>) entry.getValue();
                    if (list.size() < 5) {
                        final InputStream xmlResult = ctaConnect.connect(TRAIN_ARRIVALS, trainParams);
                        trainArrivals = xmlParser.parseArrivals(xmlResult, DataHolder.getInstance().getTrainData());
                    } else {
                        final int size = list.size();
                        int start = 0;
                        int end = 4;
                        while (end < size + 1) {
                            final List<String> subList = list.subList(start, end);
                            final MultiValuedMap<String, String> paramsTemp = new ArrayListValuedHashMap<>();
                            for (final String sub : subList) {
                                paramsTemp.put(key, sub);
                            }
                            final InputStream xmlResult = ctaConnect.connect(TRAIN_ARRIVALS, paramsTemp);
                            final SparseArray<TrainArrival> temp = xmlParser.parseArrivals(xmlResult, DataHolder.getInstance().getTrainData());
                            for (int j = 0; j < temp.size(); j++) {
                                trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                            }
                            start = end;
                            if (end + 3 >= size - 1 && end != size) {
                                end = size;
                            } else {
                                end = end + 3;
                            }
                        }
                    }
                }
            }

            // Apply filters
            int index = 0;
            while (index < trainArrivals.size()) {
                final TrainArrival arri = trainArrivals.valueAt(index++);
                final List<Eta> etas = arri.getEtas();
                // Copy data into new list to be able to avoid looping on a list that we want to modify
                final List<Eta> etas2 = new ArrayList<>();
                etas2.addAll(etas);
                int j = 0;
                for (int i = 0; i < etas2.size(); i++) {
                    final Eta eta = etas2.get(i);
                    final Station station = eta.getStation();
                    final TrainLine line = eta.getRouteName();
                    final TrainDirection direction = eta.getStop().getDirection();
                    final boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
                    if (!toRemove) {
                        etas.remove(i - j++);
                    }
                }

                // Sort Eta by arriving time
                Collections.sort(etas);
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
        return trainArrivals;
    }

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

    @Override
    public List<BikeStation> loadAllBikes() {
        try {
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            final List<BikeStation> bikeStations = jsonParser.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            return bikeStations;
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

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

    @Override
    public BusData loadLocalBusData() {
        BusData.getInstance().readBusStops();
        return BusData.getInstance();
    }

    @Override
    public TrainData loadLocalTrainData() {
        TrainData.getInstance().read();
        return TrainData.getInstance();
    }

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
}
