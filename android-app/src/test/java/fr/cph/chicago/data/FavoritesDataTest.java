package fr.cph.chicago.data;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.entity.BusArrival;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FavoritesDataTest {

    private static final String ROUTE_ID = "4";

    @Mock
    private Preferences preferences;

    @Mock
    private Context context;

    private FavoritesData favoritesData;

    @Before
    public void setUp() {
        favoritesData = FavoritesData.INSTANCE;
        favoritesData.setBusArrivals(createBusArrivals());
        favoritesData.setBusFavorites(createListFavorites());
        favoritesData.setPreferences(preferences);

        when(preferences.getBusStopNameMapping(context, "2893")).thenReturn("111th Street & Vernon");
    }

    @Test
    public void testGetBusArrivalsMappedEmptyNoFavorites() {
        favoritesData.setBusArrivals(new ArrayList<>());
        favoritesData.setBusFavorites(new ArrayList<>());

        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);
        assertNotNull(actual);
        assertThat(actual.size(), is(0));
    }

    @Test
    public void testGetBusArrivalsMappedEmptyFavorites() {
        favoritesData.setBusArrivals(new ArrayList<>());

        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);
        assertNotNull(actual);
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testGetBusArrivalsMappedWithResult() {
        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);
        assertNotNull(actual);
        assertThat(actual.size(), is(1));
        assertThat(actual, hasKey("111th Street & Vernon"));
    }


    private List<BusArrival> createBusArrivals() {
        final BusArrival busArrival = BusArrival.builder()
            .timeStamp(new Date())
            .stopName("111th Street & Vernon")
            .stopId(2893)
            .busId(1293)
            .routeId(ROUTE_ID)
            .routeDirection("Northbound")
            .busDestination("Illinois Center")
            .predictionTime(new Date())
            .isDly(false)
            .build();
        final List<BusArrival> busArrivals = new ArrayList<>();
        busArrivals.add(busArrival);
        return busArrivals;
    }

    //4_2893_Northbound

    private List<String> createListFavorites() {
        return Arrays.asList("4_2893_Northbound");
    }
}
