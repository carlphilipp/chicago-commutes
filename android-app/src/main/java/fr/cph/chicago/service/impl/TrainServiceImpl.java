package fr.cph.chicago.service.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.client.CtaClient;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.util.Util;
import io.reactivex.exceptions.Exceptions;

import static fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS;

public enum TrainServiceImpl implements TrainService {
    INSTANCE;

    @NonNull
    @Override
    public SparseArray<TrainArrival> loadFavoritesTrain(@NonNull final Context context) {
        final MultiValuedMap<String, String> trainParams = Util.getFavoritesTrainParams(context);
        SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        try {
            for (final Map.Entry<String, Collection<String>> entry : trainParams.asMap().entrySet()) {
                final String key = entry.getKey();
                if ("mapid".equals(key)) {
                    final List<String> list = (List<String>) entry.getValue();
                    if (list.size() < 5) {
                        final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(TRAIN_ARRIVALS, trainParams);
                        trainArrivals = XmlParser.Companion.getINSTANCE().parseArrivals(xmlResult, DataHolder.INSTANCE.getTrainData());
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
                            final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(TRAIN_ARRIVALS, paramsTemp);
                            final SparseArray<TrainArrival> temp = XmlParser.Companion.getINSTANCE().parseArrivals(xmlResult, DataHolder.INSTANCE.getTrainData());
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
                final TrainArrival trainArrival = trainArrivals.valueAt(index++);
                final List<Eta> etas = trainArrival.getEtas();
                trainArrival.setEtas(Stream.of(etas)
                    .filter(eta -> {
                        final TrainLine line = eta.getRouteName();
                        final TrainDirection direction = eta.getStop().getDirection();
                        return PreferencesImpl.INSTANCE.getTrainFilter(context, eta.getStation().getId(), line, direction);
                    })
                    .sorted()
                    .collect(Collectors.toList())
                );
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
        return trainArrivals;
    }

    @NonNull
    @Override
    public TrainData loadLocalTrainData(@NonNull final Context context) {
        TrainData.INSTANCE.read(context);
        return TrainData.INSTANCE;
    }

    @NonNull
    @Override
    public Optional<TrainArrival> loadStationTrainArrival(@NonNull final Context context, int stationId) {
        try {
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            params.put(context.getString(R.string.request_map_id), Integer.toString(stationId));

            final InputStream xmlResult = CtaClient.Companion.getINSTANCE().connect(TRAIN_ARRIVALS, params);
            final SparseArray<TrainArrival> arrivals = XmlParser.Companion.getINSTANCE().parseArrivals(xmlResult, TrainData.INSTANCE);
            return arrivals.size() == 1
                ? Optional.of(arrivals.get(stationId))
                : Optional.empty();
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
    }
}
