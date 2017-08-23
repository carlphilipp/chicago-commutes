package fr.cph.chicago.service.impl

import android.content.Context
import android.util.SparseArray
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS
import fr.cph.chicago.entity.Station
import fr.cph.chicago.entity.TrainArrival
import fr.cph.chicago.parser.XmlParser
import fr.cph.chicago.repository.PreferenceRepository
import fr.cph.chicago.repository.TrainRepository
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.exceptions.Exceptions
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap

object TrainServiceImpl : TrainService {

    override fun loadFavoritesTrain(context: Context): SparseArray<TrainArrival> {
        val trainParams = Util.getFavoritesTrainParams(context)
        var trainArrivals = SparseArray<TrainArrival>()
        try {
            for ((key, value) in trainParams.asMap()) {
                if ("mapid" == key) {
                    val list = value as List<String>
                    if (list.size < 5) {
                        val xmlResult = CtaClient.connect(TRAIN_ARRIVALS, trainParams)
                        trainArrivals = XmlParser.parseArrivals(xmlResult, TrainRepository)
                    } else {
                        val size = list.size
                        var start = 0
                        var end = 4
                        while (end < size + 1) {
                            val subList = list.subList(start, end)
                            val paramsTemp = ArrayListValuedHashMap<String, String>()
                            for (sub in subList) {
                                paramsTemp.put(key, sub)
                            }
                            val xmlResult = CtaClient.connect(TRAIN_ARRIVALS, paramsTemp)
                            val temp = XmlParser.parseArrivals(xmlResult, TrainRepository)
                            for (j in 0..temp.size() - 1) {
                                trainArrivals.put(temp.keyAt(j), temp.valueAt(j))
                            }
                            start = end
                            if (end + 3 >= size - 1 && end != size) {
                                end = size
                            } else {
                                end += 3
                            }
                        }
                    }
                }
            }

            // Apply filters
            var index = 0
            while (index < trainArrivals.size()) {
                val trainArrival = trainArrivals.valueAt(index++)
                val etas = trainArrival.etas
                trainArrival.etas = etas
                    .filter { (station, stop, line) ->
                        val direction = stop.direction
                        PreferenceRepository.getTrainFilter(context, station.id, line, direction)
                    }
                    .sorted()
                    .toMutableList()
            }
        } catch (e: Throwable) {
            throw Exceptions.propagate(e)
        }
        return trainArrivals
    }

    override fun loadLocalTrainData(context: Context): SparseArray<Station> {
        // Force loading train from CSV toi avoid doing it later
        return TrainRepository.stations
    }

    override fun loadStationTrainArrival(context: Context, stationId: Int): TrainArrival {
        try {
            val params = ArrayListValuedHashMap<String, String>()
            params.put(context.getString(R.string.request_map_id), Integer.toString(stationId))

            val xmlResult = CtaClient.connect(TRAIN_ARRIVALS, params)
            val arrivals = XmlParser.parseArrivals(xmlResult, TrainRepository)
            return if (arrivals.size() == 1)
                arrivals.get(stationId)
            else
                TrainArrival.buildEmptyTrainArrival()
        } catch (e: Throwable) {
            throw Exceptions.propagate(e)
        }
    }
}
