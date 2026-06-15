package org.SportsIn.strava;

import org.SportsIn.services.StravaPolylineDecoder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le décodeur de polylines Google/Strava.
 * Les valeurs de référence sont issues de la spécification officielle :
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
class StravaPolylineDecoderTest {

    private final StravaPolylineDecoder decoder = new StravaPolylineDecoder();

    @Test
    void decode_null_returnsEmptyList() {
        List<StravaPolylineDecoder.LatLng> result = decoder.decode(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void decode_blankString_returnsEmptyList() {
        List<StravaPolylineDecoder.LatLng> result = decoder.decode("   ");
        assertTrue(result.isEmpty());
    }

    /**
     * Exemple officiel Google : encode un seul point (38.5, -120.2).
     * Encodage vérifié : "_p~iF~ps|U"
     */
    @Test
    void decode_singlePoint_googleExample() {
        String encoded = "_p~iF~ps|U";
        List<StravaPolylineDecoder.LatLng> points = decoder.decode(encoded);

        assertEquals(1, points.size());
        assertEquals(38.5, points.get(0).lat(), 0.00001);
        assertEquals(-120.2, points.get(0).lng(), 0.00001);
    }

    /**
     * Exemple officiel Google à 3 points :
     * (38.5, -120.2), (40.7, -120.95), (43.252, -126.453)
     */
    @Test
    void decode_threePoints_googleExample() {
        String encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        List<StravaPolylineDecoder.LatLng> points = decoder.decode(encoded);

        assertEquals(3, points.size());

        assertEquals(38.5,    points.get(0).lat(), 0.00001);
        assertEquals(-120.2,  points.get(0).lng(), 0.00001);
        assertEquals(40.7,    points.get(1).lat(), 0.00001);
        assertEquals(-120.95, points.get(1).lng(), 0.00001);
        assertEquals(43.252,  points.get(2).lat(), 0.00001);
        assertEquals(-126.453,points.get(2).lng(), 0.00001);
    }

    @Test
    void decode_allPoints_withinValidGpsRange() {
        String encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        List<StravaPolylineDecoder.LatLng> points = decoder.decode(encoded);

        for (StravaPolylineDecoder.LatLng pt : points) {
            assertTrue(pt.lat() >= -90 && pt.lat() <= 90,
                    "Latitude hors plage : " + pt.lat());
            assertTrue(pt.lng() >= -180 && pt.lng() <= 180,
                    "Longitude hors plage : " + pt.lng());
        }
    }

    @Test
    void decode_negativeCoordinates_decodedCorrectly() {
        // Le second point de l'exemple a lat positive et lng négative
        String encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        List<StravaPolylineDecoder.LatLng> points = decoder.decode(encoded);
        assertTrue(points.get(0).lng() < 0);
        assertTrue(points.get(2).lng() < -120);
    }

    @Test
    void decode_emptyPolyline_returnsEmptyList() {
        List<StravaPolylineDecoder.LatLng> points = decoder.decode("");
        assertTrue(points.isEmpty());
    }
}
