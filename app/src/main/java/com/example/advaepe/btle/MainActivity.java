package com.example.advaepe.btle;
// ---------Autor: Adrian Vaello---------
// ---------22/10/2020---------
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.advaepe.btle.utilidades.Utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static String ETIQUETA_LOG = ">>>>";
    private int REQUEST_ENABLE_BT = 1;

    private Location mejorLocaliz;
    private LocationManager manejador;
    private static final long DOS_MINUTOS = 2 * 60 * 1000;
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    int SOLICITUD_PERMISO_LOCALIZACION = 0;
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public GeoPunto posicionActual = new GeoPunto(0, 0);

    public byte[] major;
    public byte[] minor;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothAdapter.LeScanCallback callbackLeScan = null;


    TextView textViewUbicacion;
    TextView textViewValores;

    Handler hand= new Handler();
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {

        this.callbackLeScan = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
                TramaIBeacon tib = new TramaIBeacon(bytes);
                //
                //  se ha encontrado un dispositivo
                //


                mostrarInformacionDispositivoBTLE(bluetoothDevice, rssi, bytes);


            } // onLeScan()
        }; // new LeScanCallback

        //
        boolean resultado = BluetoothAdapter.getDefaultAdapter().startLeScan(this.callbackLeScan);

    } // ()


    /* TramaIBeacon ---->
                        haLlegadoUnBeacon()
                                            ---->
     */
    public void haLlegadoUnBeacon(TramaIBeacon tib) {
        if (Utilidades.bytesToString(tib.getUUID()).equals("EPSG-GTI-PROY-G2")) {

            Log.d(ETIQUETA_LOG, " ha llegado un beacon!! ");
            extraerMediciones(tib);
            getPosicionGPS();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String momento = date.toString();

            String ubicacion = String.valueOf(posicionActual.getLatitud()) + String.valueOf(posicionActual.getLongitud());

            anyadirLecturasBD(momento, ubicacion, Utilidades.bytesToInt(minor), "Azufre");

            //http://localhost:81/WebSprint0/registro.php?momento=hoy&ubicacion=casa&valor=3&idMagnitud=Azufre
            new CargarDatos().execute("http://192.168.43.189:81/WebSprint0/registro.php?momento=" + momento + "&ubicacion=" + ubicacion + "&valor=" + Utilidades.bytesToInt(minor) + "&idMagnitud=Azufre");


        }
    }
    // --------------------------------------------------------------
    // --------------------------------------------------------------

    /* TramaIBeacon ---->
                     extraerMediciones()
                                            ---->
     */
    public void extraerMediciones(TramaIBeacon tib) {
        major = tib.getMajor();
        minor = tib.getMinor();
        Log.d(ETIQUETA_LOG, "////////Major: " + Utilidades.bytesToInt(major) + " minor: " + Utilidades.bytesToInt(minor));
        textViewValores.setText("Major: " + Utilidades.bytesToInt(major) + "  Minor: " + Utilidades.bytesToInt(minor));


    }


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final UUID dispositivoBuscado) {
        this.callbackLeScan = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

                //
                // dispostivo encontrado
                //
                Log.d(ETIQUETA_LOG, " EStoy escaneando");
                TramaIBeacon tib = new TramaIBeacon(bytes);
                haLlegadoUnBeacon(tib);

                String uuidString = Utilidades.bytesToString(tib.getUUID());

                if (uuidString.compareTo(Utilidades.uuidToString(dispositivoBuscado)) == 0) {

                    mostrarInformacionDispositivoBTLE(bluetoothDevice, rssi, bytes);
                } else {
                    Log.d(MainActivity.ETIQUETA_LOG, " * UUID buscado >" +
                            Utilidades.uuidToString(dispositivoBuscado) + "< no concuerda con este uuid = >" + uuidString + "<");
                }

            } // onLeScan()
        }; // new LeScanCallback

        BluetoothAdapter.getDefaultAdapter().startLeScan(this.callbackLeScan);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*private void detenerBusquedaDispositivosBTLE() {
        if (this.callbackLeScan == null) {
            Log.d(ETIQUETA_LOG, "*****callbackLesScan==null");
            return;
        }
        Log.d(ETIQUETA_LOG, "*****antes de cargar datos");
        //new CargarDatos().execute("http://192.168.1.16:81/WebSprint0/registro.php?momento=manana&ubicacion=dani&valor=2&idMagnitud=Azufre");
        //
        //
        //
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this.callbackLeScan);
        this.callbackLeScan = null;

       /* DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String momento = date.toString();

        String ubicacion = String.valueOf(posicionActual.getLatitud()) + String.valueOf(posicionActual.getLongitud());*/

    //}
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
        //anyadirLecturasBD("momento", "ubicacion", 1, "Azufre");
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        this.buscarEsteDispositivoBTLE(Utilidades.stringToUUID("EPSG-GTI-PROY-GD"));
        //anyadirLecturasBD("momento", "ubicacion", 1, "Azufre");
    } // ()*/

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detenerdispositivo BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    }*/ // ()

    /* Text,Text,N,Text ---->
                       anyadirLecturasBD()
                                              ---->
       */
    public void anyadirLecturasBD(String momento, String ubicacion, int valor, String idMagnitud) {

        ConexionSQLite conn = new ConexionSQLite(this, "lecturas", null, 1);

        SQLiteDatabase db = conn.getWritableDatabase();

        String insert = "INSERT INTO " + Utilidades.TABLA_LECTURAS + "(" + Utilidades.CAMPO_MOMENTO + ","
                + Utilidades.CAMPO_UBICACION + ","
                + Utilidades.CAMPO_VALOR + ","
                + Utilidades.CAMPO_IDMAGNITUD + ")" + " VALUES ( '" + momento + "','" + ubicacion + "'," + valor + ",'" + idMagnitud + "')";
        Log.d(ETIQUETA_LOG, " ++++Datos añadidos correctamente");
        db.execSQL(insert);
        db.close();
    }

    /*  ---->
              getPosicionGPS()
                              ---->
     */
    public void getPosicionGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER));
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER));
            } else {
                solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION,
                        "Sin el permiso localización no puedo mostrar la distancia" +
                                " a los lugares.", SOLICITUD_PERMISO_LOCALIZACION, this);
            }
        }
    }

    /* Texto, Texto, N, Activity ---->
                            solicitarPermiso()
                                            ---->
     */
    public static void solicitarPermiso(final String permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad,
                permiso)) {
            new android.app.AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad,
                    new String[]{permiso}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPosicionGPS();
                activarProveedores();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activarProveedores();
    }

    /* ---->
             activarProveedores()
                                    ---->
    */
    private void activarProveedores() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        20 * 1000, 5, (LocationListener) this);
            }
            /*if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        10 * 1000, 10, (LocationListener) this);
            }*/
        } else {
            solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION,
                    "Sin el permiso localización no puedes recibir datos", SOLICITUD_PERMISO_LOCALIZACION, this);
        }
    }

    /* Location ---->
                     actualizaMejorLocaliz()
                                            ---->
     */
    private void actualizaMejorLocaliz(Location localiz) {

        if (localiz != null && (mejorLocaliz == null
                || localiz.getAccuracy() < 2 * mejorLocaliz.getAccuracy()
                || localiz.getTime() - mejorLocaliz.getTime() > DOS_MINUTOS)) {

            mejorLocaliz = localiz;

            posicionActual.setLatitud(localiz.getLatitude());
            posicionActual.setLongitud(localiz.getLongitude());

            TextView textView = findViewById(R.id.textView);
            textView.setText("Latitud: " + posicionActual.getLatitud() + "Longitud: " + posicionActual.getLongitud());

            Log.d(ETIQUETA_LOG, "+++++++Latitud: " + posicionActual.getLatitud() + "Longitud: " + posicionActual.getLongitud());

        }
    }

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

        textViewUbicacion = findViewById(R.id.textView);
        textViewValores = findViewById(R.id.textView2);
        //CONEXION BD
        hand.removeCallbacks(actualizar);
        hand.postDelayed(actualizar,1);

        ConexionSQLite conn = new ConexionSQLite(this, "lecturas.db", null, 1);
        buscarEsteDispositivoBTLE(Utilidades.stringToUUID("EPSG-GTI-PROY-G2"));
    } // onCreate()

    /* Text ---->
                    downloadUrl()
                                  ---->Text
    */
    private String downloadUrl(String myurl) throws IOException {
        InputStream stream = null;

        int len = 500;
        Log.d(ETIQUETA_LOG, "****downloadUrl" + myurl);
        try {
            URL url = new URL(myurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();

            int response = connection.getResponseCode();
            Toast.makeText(getApplicationContext(), "Response:" + response, Toast.LENGTH_LONG).show();

            stream = connection.getInputStream();

            String contentAsString = leer(stream, len);
            connection.disconnect();
            return contentAsString;
        } catch (IOException e) {

            Log.d(ETIQUETA_LOG, "****estoy en catch=" + e);

        } finally {
            if (stream != null) {
                stream.close();
                Log.d("respuesta", "Cerrada: ");
            }
            return "";
        }

    }

    public String leer(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private class CargarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            //El parametro urls[0] es la url deseada

            Log.d(ETIQUETA_LOG, "****urls=" + urls[0]);

            try {
                Log.d(ETIQUETA_LOG, "****estoy en try" + urls[0]);
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }


        @Override
        protected void onPostExecute(String result) {
            Log.d(ETIQUETA_LOG, "-----result=" + result);
            //Toast.makeText(getApplicationContext(), "Se almacenaron los datos correctamente", Toast.LENGTH_LONG).show();

        }
    }

    //funcion para que actualize la pagina al aceptar la ubicacion y bluethooth
    private Runnable actualizar= new Runnable() {
        @Override
        public void run() {
        buscarEsteDispositivoBTLE(Utilidades.stringToUUID("EPSG-GTI-PROY-G2"));
       // getPosicionGPS();
        hand.postDelayed(this,1000);
        }
    };
} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------

