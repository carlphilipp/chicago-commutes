package fr.cph.chicago.data;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.entity.BusArrival;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

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
        // Given
        favoritesData = FavoritesData.INSTANCE;
        favoritesData.setBusArrivals(createBusArrivals());
        favoritesData.setBusFavorites(createListFavorites());
        favoritesData.setPreferences(preferences);
        given(preferences.getBusStopNameMapping(context, "2893")).willReturn("111th Street & Vernon");
    }

    @Test
    public void testGetBusArrivalsMappedEmptyNoFavorites() {
        // Given
        favoritesData.setBusArrivals(Collections.emptyList());
        favoritesData.setBusFavorites(Collections.emptyList());

        // When
        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);

        // Then
        assertNotNull(actual);
        assertThat(actual.size(), is(0));
    }

    @Test
    public void testGetBusArrivalsMappedEmptyFavorites() {
        // Given
        favoritesData.setBusArrivals(Collections.emptyList());

        // When
        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);

        // Then
        assertNotNull(actual);
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testGetBusArrivalsMappedWithResult() {
        // When
        Map<String, Map<String, List<BusArrival>>> actual = favoritesData.getBusArrivalsMapped(ROUTE_ID, context);

        // Then
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
        return Collections.singletonList(busArrival);
    }

    private List<String> createListFavorites() {
        return Collections.singletonList("4_2893_Northbound");
    }
}
