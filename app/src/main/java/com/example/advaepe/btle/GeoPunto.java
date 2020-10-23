package com.example.advaepe.btle;

public class GeoPunto {
    public double latitud;
    public double longitud;
    public GeoPunto posicionActual;

    public GeoPunto(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

         /*       ---->
                        getLatitud()
                                     ---->Double
        */
    public double getLatitud() {
        return latitud;
    }


         /* Double ---->
                        setLatitud()
                                     ---->
        */

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

        /*        ---->
                        getLongitud()
                                     ---->Double
        */

    public double getLongitud() {
        return longitud;
    }

     /* Double ---->
                        setLatitud()
                                     ---->
        */

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }


    /* Geopunto ---->
                       setLatitud()
                                    ---->double
       */
    public double distancia(GeoPunto punto) {
        final double RADIO_TIERRA = 6371000; // en metros
        double dLat = Math.toRadians(latitud - punto.latitud);
        double dLon = Math.toRadians(longitud - punto.longitud);
        double lat1 = Math.toRadians(punto.latitud);
        double lat2 = Math.toRadians(latitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                        Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * RADIO_TIERRA;
    }

     /*     ---->
                 getPosicionAcual()
                                     ---->Geopunto
        */

    public GeoPunto getPosicionActual() {
        return posicionActual;
    }

     /* Geopunto ---->
                        setPosicionActual()
                                            ---->
        */

    public void setPosicionActual(GeoPunto posicionActual) {
        this.posicionActual = posicionActual;
    }
}
