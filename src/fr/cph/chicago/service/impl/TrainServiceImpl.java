package fr.cph.chicago.service.impl;

import android.util.SparseArray;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;
import rx.exceptions.Exceptions;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

public class TrainServiceImpl implements TrainService {

    private final XmlParser xmlParser;

    public TrainServiceImpl() {
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
    public TrainData loadLocalTrainData() {
        TrainData.getInstance().read();
        return TrainData.getInstance();
    }
}
