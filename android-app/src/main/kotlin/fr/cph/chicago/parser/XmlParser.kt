/**
 * Copyright 2018 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.parser

import android.util.SparseArray
import fr.cph.chicago.core.model.*
import fr.cph.chicago.entity.*
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * XML parser
 * @author Carl-Philipp Harmant
 *
 * @version 1
 */
// TODO to refactor and optimize
object XmlParser {

    private val trainService: TrainService = TrainService
    private val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
    private val simpleDateFormatTrain: SimpleDateFormat = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US)
    private val simpleDateFormatBus: SimpleDateFormat = SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)

    /**
     * Parse arrivals
     * @param inputStream   the xml string
     * @return a list of train arrival
     * @throws ParserException the parser exception
     */
    @Synchronized
    @Throws(ParserException::class)
    fun parseTrainArrivals(inputStream: InputStream): SparseArray<TrainArrival> {
        val result = SparseArray<TrainArrival>()
        try {

            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType
            var tagName: String? = null

            var stationId: Int? = null
            var stopId = 0
            var stationName: String? = null
            var stopDestination: String? = null
            var routeName: TrainLine? = null
            var destinationName: String? = null
            var predictionDate: Date? = null
            var arrivalDepartureDate: Date? = null
            var isApp = false
            var isDly = false
            val latitude = 0.0
            val longitude = 0.0

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.name
                } else if (eventType == XmlPullParser.END_TAG) {
                    val etaName = parser.name
                    if (StringUtils.isNotBlank(etaName) && "eta" == etaName) {
                        // FIXME analyze why we have that train service here. Should not.
                        val station = trainService.getStation(stationId!!)
                        station.name = stationName!!
                        val stop = trainService.getStop(stopId)
                        stop.description = stopDestination!!
                        val position = Position(latitude, longitude)
                        // FIXME that should not be done here
                        destinationName =
                            if ("See train".equals(destinationName, ignoreCase = true) && stop.description.contains("Loop") && routeName == TrainLine.GREEN ||
                                "See train".equals(destinationName, ignoreCase = true) && stop.description.contains("Loop") && routeName == TrainLine.BROWN ||
                                "Loop, Midway".equals(destinationName, ignoreCase = true) && routeName == TrainLine.BROWN)
                                "Loop"
                            else
                                destinationName
                        val eta = TrainEta(station, stop, routeName!!, destinationName!!, predictionDate!!, arrivalDepartureDate!!, isApp, isDly, position)
                        val arri = result.get(stationId, TrainArrival.buildEmptyTrainArrival())
                        val etas = arri.trainEtas
                        etas.add(eta)
                        result.append(stationId, arri)
                    }
                    tagName = null
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    if (tagName != null) {
                        when (tagName) {
                            "staId" -> stationId = text.toInt()
                            "stpId" -> stopId = text.toInt()
                            "staNm" -> stationName = text
                            "stpDe" -> stopDestination = text
                            "rt" -> routeName = TrainLine.fromXmlString(text)
                            "destNm" -> destinationName = text
                            "prdt" -> predictionDate = simpleDateFormatTrain.parse(text)
                            "arrT" -> arrivalDepartureDate = simpleDateFormatTrain.parse(text)
                            "isApp" -> isApp = text.toBoolean()
                            "isDly" -> isDly = text.toBoolean()
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } catch (e: ParseException) {
            throw ParserException(e)
        } catch (e: IOException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(inputStream)
        }
        return result
    }

    /**
     * Parse bus directions
     *
     * @param xml the xml to parse
     * @param id  the line id
     * @return a bus directions
     * @throws ParserException a parser exception
     */
    @Synchronized
    @Throws(ParserException::class)
    fun parseBusDirections(xml: InputStream, id: String): BusDirections {
        val result = BusDirections(id)
        try {
            parser.setInput(xml, "UTF-8")
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    val busDirection = BusDirection.fromString(text)
                    if (busDirection != BusDirection.UNKNOWN) {
                        result.addBusDirection(busDirection)
                    }
                }
                eventType = parser.next()
            }
        } catch (e: IOException) {
            throw ParserException(e)
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(xml)
        }
        return result
    }

    /**
     * Parse bus bounds
     *
     * @param xml the xml to parse
     * @return a list of bus stop
     * @throws ParserException a parser exception
     */
    @Synchronized
    @Throws(ParserException::class)
    fun parseBusBounds(xml: InputStream): List<BusStop> {
        val result = mutableListOf<BusStop>()
        try {
            parser.setInput(xml, "UTF-8")
            var eventType = parser.eventType
            var tagName: String? = null

            var stopId: Int? = null
            var stopName: String? = null
            var latitude: Double? = null
            var longitude: Double? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.name
                } else if (eventType == XmlPullParser.END_TAG) {
                    val stop = parser.name
                    if (StringUtils.isNotBlank(stop) && "stop" == stop) {
                        val busArrival = BusStop(stopId!!, stopName!!, stopName, Position(latitude!!, longitude!!))
                        result.add(busArrival)
                    }
                    tagName = null
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    if (tagName != null) {
                        when (tagName) {
                            "stpid" -> stopId = text.toInt()
                            "stpnm" -> stopName = text
                            "lat" -> latitude = text.toDouble()
                            "lon" -> longitude = text.toDouble()
                            "msg" -> throw ParserException(text)
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: IOException) {
            throw ParserException(e)
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(xml)
        }
        return result
    }

    /**
     * Parse alert general
     *
     * @param xml the xml to parse
     * @return a list of alert
     * @throws ParserException a parser exception
     */
    @Synchronized
    @Throws(ParserException::class)
    fun parsePatterns(xml: InputStream): List<BusPattern> {
        val result = mutableListOf<BusPattern>()
        try {
            parser.setInput(xml, "UTF-8")
            var eventType = parser.eventType
            var tagName: String? = null

            // Pattern
            var direction: String? = null
            var points: MutableList<PatternPoint> = mutableListOf()
            // Point
            var latitude = 0.0
            var longitude = 0.0
            var type: String? = null
            var stopName: String? = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.name
                } else if (eventType == XmlPullParser.END_TAG) {
                    val current = parser.name
                    if (StringUtils.isNotBlank(current) && "ptr" == current) {
                        val busPattern = BusPattern(direction!!, ArrayList(points))
                        result.add(busPattern)

                        points = mutableListOf()
                    } else if (StringUtils.isNotBlank(current) && "pt" == current) {
                        val patternPoint = PatternPoint(
                            Position(latitude, longitude), type!!, stopName!!
                        )
                        points.add(patternPoint)
                        latitude = 0.0
                        longitude = 0.0
                        type = null
                        stopName = ""
                    }
                    tagName = null
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    if (tagName != null) {
                        when (tagName) {
                            "rtdir" -> direction = BusDirection.fromString(text).text
                            "lat" -> latitude = text.toDouble()
                            "lon" -> longitude = text.toDouble()
                            "typ" -> type = text
                            "stpnm" -> stopName = text
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } catch (e: IOException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(xml)
        }
        return result
    }

    @Synchronized
    @Throws(ParserException::class)
    fun parseVehicles(inputStream: InputStream): List<Bus> {
        val buses = mutableListOf<Bus>()
        try {
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType
            var tagName: String? = null

            var busId: Int? = null
            var latitude: Double? = null
            var longitude: Double? = null
            var heading: Int? = null
            var destination: String? = null


            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.name
                } else if (eventType == XmlPullParser.END_TAG) {
                    val vehicle = parser.name
                    if (StringUtils.isNotBlank(vehicle) && "vehicle" == vehicle) {
                        val position = Position(latitude!!, longitude!!)
                        val bus = Bus(busId!!, position, heading!!, destination!!)
                        buses.add(bus)
                    }
                    tagName = null
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    if (tagName != null) {
                        when (tagName) {
                            "vid" -> busId = text.toInt()
                            "lat" -> latitude = text.toDouble()
                            "lon" -> longitude = text.toDouble()
                            "hdg" -> heading = text.toInt()
                            "des" -> destination = text
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } catch (e: IOException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(inputStream)
        }
        return buses
    }

    @Synchronized
    @Throws(ParserException::class)
    fun parseTrainsLocation(inputStream: InputStream): List<Train> {
        val trains = mutableListOf<Train>()
        try {
            parser.setInput(inputStream, "UTF-8")
            var tagName: String? = null
            var eventType = parser.eventType

            var routeNumber = 0
            var destName: String? = null
            var app = false
            var latitude = 0.0
            var longitude = 0.0
            var heading = 0

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.name
                } else if (eventType == XmlPullParser.END_TAG) {
                    val trainNode = parser.name
                    if (StringUtils.isNotBlank(trainNode) && "train" == trainNode) {
                        val train = Train(routeNumber, destName!!, app, Position(latitude, longitude), heading)
                        trains.add(train)
                    }
                    tagName = null
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = parser.text
                    if (tagName != null) {
                        when (tagName) {
                            "rn" -> routeNumber = text.toInt()
                            "destNm" -> destName = text
                            "lat" -> latitude = text.toDouble()
                            "lon" -> longitude = text.toDouble()
                            "heading" -> heading = text.toInt()
                            "isApp" -> app = text.toBoolean()
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw ParserException(e)
        } catch (e: IOException) {
            throw ParserException(e)
        } finally {
            Util.closeQuietly(inputStream)
        }
        return trains
    }

    @Synchronized
    @Throws(ParserException::class)
    fun parseTrainsFollow(inputStream: InputStream): MutableList<TrainEta> {
        val arrivals = parseTrainArrivals(inputStream)
        val res = mutableListOf<TrainEta>()
        var index = 0
        while (index < arrivals.size()) {
            val (etas) = arrivals.valueAt(index++)
            if (etas.size != 0) {
                res.add(etas[0])
            }
        }
        res.sort()
        return res
    }
}
