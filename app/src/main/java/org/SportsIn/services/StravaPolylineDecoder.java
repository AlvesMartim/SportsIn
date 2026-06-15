package org.SportsIn.services;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Décode les polylines encodées Google/Strava vers une liste de points GPS.
 * Algorithme : https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
@Component
public class StravaPolylineDecoder {

    public record LatLng(double lat, double lng) {}

    /**
     * Décode une polyline encodée en liste de coordonnées GPS.
     * @param encoded chaîne encodée (peut être null/vide)
     * @return liste de points, vide si encoded est null ou vide
     */
    public List<LatLng> decode(String encoded) {
        List<LatLng> points = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) return points;

        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 0;
            int shift = 0;
            int b;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            result = 0;
            shift = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            points.add(new LatLng(lat / 1e5, lng / 1e5));
        }

        return points;
    }
}
