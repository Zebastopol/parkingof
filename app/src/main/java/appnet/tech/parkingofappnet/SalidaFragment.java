package appnet.tech.parkingofappnet;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SalidaFragment extends Fragment {
    View salida;
    public Button btnvolver, btnsalida,btnsumup;
    TextView tvpatente, tvfechaentrada, tvvuelto, tvsalida, tvminutos, tvtotal;
    EditText etefectivo;
    ConexionSQLiteHelper conne;
    boolean issumup=false;
    String txcode,nombrevendedor;
    HashMap<String, String> hstipos = new HashMap<>();
    ArrayList<String> listadoc = new ArrayList<>();
    ArrayList<String> tipos = new ArrayList<>();
    Vehiculo vehiculo;
    String token, bodega, respuesta, folio;
    Spinner spdoc, spformapago;
    int tipocobro, valor,cobrobase,mincobrobase;
    double total, neto, iva;
    Date dateentrada, datesalida;

    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    SunmiPrinterService sunmiPrinterService;
    InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
        }

        @Override
        protected void onDisconnected() {
        }
    };

    InnerResultCallbcak innerResultCallbcak = new InnerResultCallbcak() {
        @Override
        public void onRunResult(boolean isSuccess) throws RemoteException {

        }

        @Override
        public void onReturnString(String result) throws RemoteException {

        }

        @Override
        public void onRaiseException(int code, String msg) throws RemoteException {

        }

        @Override
        public void onPrintResult(int code, String msg) throws RemoteException {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        salida = inflater.inflate(R.layout.fragment_salida, container, false);
        conne = new ConexionSQLiteHelper(getContext(), "bd_appnet_parking", null, 1);
        try {
            InnerPrinterManager.getInstance().bindService(getContext(), innerPrinterCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnsalida = salida.findViewById(R.id.btnsalida);
        btnvolver = salida.findViewById(R.id.btnvolver);
        btnsumup=salida.findViewById(R.id.btnpagosumup);
        tvminutos=salida.findViewById(R.id.tvminutos);
        tvsalida = salida.findViewById(R.id.tvsalida);
        tvtotal=salida.findViewById(R.id.tvtotal);
        etefectivo = salida.findViewById(R.id.etefectivo);
        tvvuelto=salida.findViewById(R.id.tvvuelto);
        tvpatente = salida.findViewById(R.id.tvpatente);
        tvfechaentrada = salida.findViewById(R.id.tventrada);
        spdoc = salida.findViewById(R.id.spdoc);
        spformapago = salida.findViewById(R.id.spforma);

        consultarToken();
        consultarTipo();

        //listadoc.add("Boleta Electrónica");
        listadoc.add("voucher");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), R.layout.my_spinner, listadoc);
        adapter.setDropDownViewResource(R.layout.my_spinner);
        spdoc.setAdapter(adapter);

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<String>(getContext(), R.layout.my_spinner, tipos);
        adapter.setDropDownViewResource(R.layout.my_spinner);
        spformapago.setAdapter(adapter2);

        cargarVehiculo();
        calcularTarifa();

        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).cambioFragment(1);
            }
        });

        btnsalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread() {
                    ProgressDialog progressDialog = ProgressDialog.show(getContext(), "Generando Venta",
                            "Cargando, por favor espere", true);
                    @Override
                    public void run() {
                        Time today = new Time(Time.getCurrentTimezone());
                        today.setToNow();
                        final int mes = today.month + 1;
                        final int dia = today.monthDay;
                        final int anio = today.year;
                        JSONArray ja = new JSONArray();
                        JSONArray detalle = new JSONArray();
                        JSONObject jo = new JSONObject();
                        JSONObject cabezera = new JSONObject();

                        String fp ="";

                        int tipodoc=0;

                        if(spdoc.getSelectedItem().toString().equals("voucher"))
                        {
                            tipodoc=1002;
                        }else
                        {
                            tipodoc=39;
                        }

                        try {
                            cabezera.put("Total", total);
                            if (tipodoc==1002) {
                                cabezera.put("NetoExento", total);
                                cabezera.put("NetoAfecto", 0);
                                cabezera.put("IVA", 0);
                                cabezera.put("Documento", 1002);
                            } else {
                                cabezera.put("NetoExento", 0);
                                cabezera.put("NetoAfecto", neto);
                                cabezera.put("IVA", iva);
                                cabezera.put("Documento", tipodoc);


                            }
                            cabezera.put("Descuento", 0);

                            String in = String.valueOf(spformapago.getSelectedItemPosition());
                            fp = hstipos.get(in);
                            cabezera.put("formapago", fp);
                            cabezera.put("Propina", 0);

                            cabezera.put("Observacion", "");

                            cabezera.put("Bodega", bodega);
                            cabezera.put("Dia", dia);
                            cabezera.put("Mes", mes);
                            cabezera.put("Anio", anio);
                            cabezera.put("FechaVencimiento", "" + anio + "-" + mes + "-" + dia);
                            cabezera.put("Estado", 1);
                            String cltrut;
                            cltrut = "66666666-6";
                            cabezera.put("Cliente", cltrut);

                        } catch (Exception e) {
                            e.toString();
                        }

                        try {
                            JSONObject producto = new JSONObject();
                            producto.put("Item", 1);
                            producto.put("Codigo", "00000000-0");
                            producto.put("Precio", total);
                            producto.put("Cantidad", 1);
                            producto.put("Descuento", 0);
                            producto.put("Detallelargo", vehiculo.getPatente());
                            detalle.put(producto);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JSONObject parking = new JSONObject();

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String fechaingreso = sdf.format(dateentrada.getTime());
                            sdf = new SimpleDateFormat("HH:mm");
                            String horaingreso = sdf.format(dateentrada.getTime());
                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String fechasalida = sdf.format(datesalida.getTime());
                            sdf = new SimpleDateFormat("HH:mm");
                            String horasalida = sdf.format(datesalida.getTime());

                            parking.put("patente",vehiculo.getPatente());
                            parking.put("fecha_ingreso",fechaingreso);
                            parking.put("hora_ingreso",horaingreso);
                            parking.put("fecha_salida",fechasalida);
                            parking.put("hora_salida",horasalida);
                            parking.put("estado",2);

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        JSONArray japago= new JSONArray();

                        JSONObject jopago = new JSONObject();

                        try {
                            jopago.put("Formapago",fp);
                            jopago.put("Total",(int)total);
                            japago.put(jopago);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            jo.put("Encabezado", cabezera);
                            jo.put("Detalle", detalle);
                            jo.put("Parking", parking);
                            jo.put("Pago",japago);
                        } catch (JSONException e) {
                            e.toString();
                        }

                        //ja.put(jo);
                        Log.e("json", ja.toString());

                        //String test =POST ja.toString();

                        respuesta = POST(Utilidades.URL_VOUCHER, jo.toString(), token);
                        Log.d("asdasd", respuesta);
                        JSONObject resp = new JSONObject();
                        try {
                            resp = new JSONObject(respuesta);
                            String img = resp.getString("pdf417");
                            folio = resp.getString("numVenta");
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            try {
                                JSONObject jserror = new JSONObject(respuesta);
                                final String msj = jserror.getString("mensaje");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), msj, Toast.LENGTH_LONG).show();
                                    }
                                });

                            } catch (JSONException ex) {
                                ex.printStackTrace();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            return;
                        }

                        try {
                            if (!resp.getString("numVenta").equals(null)) {
                                imprimirBoleta(detalle, respuesta);
                                retirarVehiculo(vehiculo.getPatente());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Venta generada", Toast.LENGTH_LONG).show();
                                        ((MainActivity) getActivity()).cambioFragment(1);
                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error al generar venta", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                };
                thread.start();
            }
        });

        etefectivo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(etefectivo.getText().toString().length()>0)
                {
                    final int efectivo = Integer.parseInt(etefectivo.getText().toString());
                    if (efectivo>total)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int vuelto = (int) (efectivo-total);
                                DecimalFormat formatter = new DecimalFormat("#,###,###");
                                tvvuelto.setText("$"+formatter.format(vuelto));
                            }
                        });
                    }
                }
            }
        });


        btnsumup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity)getActivity()).generarsumup(Integer.parseInt(tvtotal.getText().toString()));

            }
        });


        spformapago.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spformapago.getSelectedItem().toString().equals("SumUp"))
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnsumup.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnsumup.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return salida;
    }

    private void retirarVehiculo(String patente) {

        SQLiteDatabase db = conne.getWritableDatabase();
        db.execSQL("delete from " + Utilidades.TABLA_VEHICULOS + " where " + Utilidades.CAMPO_PATENTE + " = '" + patente + "'");
    }

    public String POST(String URL, String rbody, String token) {
        try {
            RequestBody body = RequestBody.create(JSON, rbody);
            Request request = new Request.Builder()
                    .addHeader("token", token)
                    .url(URL)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hubo un problema";
    }

    private String convertInputStreamToString(InputStream input) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
        String line = "";
        String result = "";
        while ((line = buffer.readLine()) != null) {
            result += line;
        }
        input.close();
        return result;
    }

    private void imprimirBoleta(JSONArray detalle, String resp) {

        try {
            JSONObject ja;
            Bitmap decodedByte;
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            int mes = today.month + 1;
            String fecha = today.monthDay + "/" + mes + "/" + today.year;
            String hora = today.hour + ":" + today.minute + ":" + today.second;

            try {
                ja = new JSONObject(resp);

                SQLiteDatabase db = conne.getReadableDatabase();
                DecimalFormat formatter = new DecimalFormat("#,###,###");

                Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_RUTEMPRESACONF + "," + Utilidades.CAMPO_RAZONSOCIALCONF + "," + Utilidades.CAMPO_GIROCONF + "," + Utilidades.CAMPO_DIRECCIONCONF + "," + Utilidades.CAMPO_LOGOCONF + "," + Utilidades.CAMPO_RESOLUCIONSIICONF + "," + Utilidades.CAMPO_UNIDADSIICONF + " from " + Utilidades.TABLA_CONFIGURACION, null);
                cursor.moveToNext();

                sunmiPrinterService.setFontSize(23, innerResultCallbcak);

                sunmiPrinterService.printText("\n", innerResultCallbcak);
                sunmiPrinterService.setAlignment(1, innerResultCallbcak);
                sunmiPrinterService.printText(cursor.getString(1) + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Rut:" + cursor.getString(0) + "\n\n", innerResultCallbcak);
                sunmiPrinterService.printText("S.I.I.-" + cursor.getString(6) + "\n", innerResultCallbcak);
                sunmiPrinterService.printText(cursor.getString(2) + "\n", innerResultCallbcak);
                sunmiPrinterService.printText(cursor.getString(3) + "\n", innerResultCallbcak);

                String doc = "Boleta Electrónica";

                if (spdoc.getSelectedItem().equals("voucher")) {
                    doc = "Voucher";
                }
                else
                {
                    doc = "Boleta Electrónica";
                }

                sunmiPrinterService.printText(doc + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Folio:" + ja.getString("numVenta") + "\n", innerResultCallbcak);

                if (issumup)
                {
                    sunmiPrinterService.printText("Cod. Transaccion :" + txcode + "\n", innerResultCallbcak);
                }

                sunmiPrinterService.printText("Fecha:" + fecha + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Hora:" + hora + "\n\n", innerResultCallbcak);

                sunmiPrinterService.printText("Entrada: " + tvfechaentrada.getText().toString() + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Salida: " + tvsalida.getText().toString() + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Patente: " + tvpatente.getText().toString() + "\n", innerResultCallbcak);
                sunmiPrinterService.printText("Usuario: " + nombrevendedor + "\n\n", innerResultCallbcak);

                String reso = cursor.getString(5);

                sunmiPrinterService.setAlignment(0, innerResultCallbcak);

                if (!spdoc.getSelectedItem().toString().equals("voucher")) {

                    sunmiPrinterService.printText("Neto:$" + formatter.format(Math.round(neto)) + "\n", innerResultCallbcak);
                    sunmiPrinterService.printText("IVA:$" + formatter.format(Math.round(iva)) + "\n\n", innerResultCallbcak);

                }
                sunmiPrinterService.setFontSize(35, innerResultCallbcak);
                sunmiPrinterService.printText("Total:$" + formatter.format(total) + "\n\n", innerResultCallbcak);

                try {
                    String b = ja.getString("pdf417");
                    byte[] decodedString = Base64.decode(b, Base64.DEFAULT);
                    decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    decodedByte = Bitmap.createScaledBitmap(decodedByte, 400, 200, false);

                    sunmiPrinterService.printBitmap(decodedByte, innerResultCallbcak);
                    sunmiPrinterService.setAlignment(1, innerResultCallbcak);
                    sunmiPrinterService.setFontSize(23, innerResultCallbcak);
                    sunmiPrinterService.printText("\n", innerResultCallbcak);
                    sunmiPrinterService.printText("Timbre Electrónico S.I.I. \n" + "Resolución " + reso + "\n\n", innerResultCallbcak);

                } catch (Exception e) {

                }

                sunmiPrinterService.setFontSize(19,innerResultCallbcak);
                sunmiPrinterService.printText( "\n", innerResultCallbcak);
                sunmiPrinterService.printText( "Desarrollado por AppnetTech"+"\n", innerResultCallbcak);
                //sunmiPrinterService.autoOutPaper(innerResultCallbcak);

            } catch (JSONException e) {
                e.printStackTrace();
            }
           // sunmiPrinterService.autoOutPaper(innerResultCallbcak);
            //sunmiPrinterService.cutPaper(innerResultCallbcak);

        } catch (Exception e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "No es Posible Imprimir ", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    private void cargarVehiculo() {
        vehiculo = (Vehiculo) getActivity().getIntent().getSerializableExtra("vehiculo");

    }

    private void consultarTipo() {
        SQLiteDatabase db = conne.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_NOMBRETIPO + "," + Utilidades.CAMPO_IDTIPO + " from " + Utilidades.TABLA_TIPOPAGO, null);
        while (cursor.moveToNext()) {
            try {

                hstipos.put(String.valueOf(tipos.size()), cursor.getString(1));
                tipos.add(cursor.getString(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        db.close();
        cursor.close();
    }

    private void consultarToken() {

        SQLiteDatabase db = conne.getReadableDatabase();

        Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_TOKENCONF + "," + Utilidades.CAMPO_BODEGACONF  +"," + Utilidades.CAMPO_NOMBREUSERCONF+ " from " + Utilidades.TABLA_CONFIGURACION, null);
        cursor.moveToFirst();
        token = cursor.getString(0);
        bodega = cursor.getString(1);
        nombrevendedor= cursor.getString(2);

    }

    private void calcularTarifa() {
        try {
            SQLiteDatabase db = conne.getReadableDatabase();
            Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_TIPOCOBROPARKING + "," + Utilidades.CAMPO_VALORCOBROPARKING+ "," + Utilidades.CAMPO_COBROBASEPARKING +"," + Utilidades.CAMPO_MINCOBROBASEPARKING + " from " + Utilidades.TABLA_PARKING, null);
            cursor.moveToNext();

            tipocobro = cursor.getInt(0);
            valor = cursor.getInt(1);
            cobrobase = cursor.getInt(2);
            mincobrobase = cursor.getInt(3);

            cursor.close();
            db.close();

            String entrada = vehiculo.getFechaentrada()+" "+vehiculo.getHoraentrada();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateentrada = sdf.parse(entrada);


            Calendar c = Calendar.getInstance();
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            datesalida = sdf.parse(sdf.format(c.getTime()));

            final int diferenciamin= (int) getDateDiff(dateentrada, datesalida, TimeUnit.MINUTES);
            long diferencia = getDateDiff(dateentrada, datesalida, TimeUnit.MINUTES);
            int d=0;
            diferencia= diferencia-mincobrobase;

            switch (tipocobro) {
                case 1:
                    //dia

                    d = (int) (diferencia/1440);

                    diferencia = d;

                    break;
                case 2:
                    //horas
                    d = (int) (diferencia/60);

                    diferencia = d;
                    break;
                case 3:
                    //minutos

                    break;

                case 4:
                    //minutos
                    diferencia = getDateDiff(dateentrada, datesalida, TimeUnit.MINUTES);

                    d = (int) (diferencia/30);

                    diferencia = d;

                    break;
            }

            total = (valor * diferencia)+cobrobase;
            neto = ((total / 119) * 100);
            iva = total - neto;


            neto = round(neto, 2);
            iva = round(iva, 2);

            final long finalDiferencia = diferencia;
            final String salida = sdf.format(c.getTime());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvminutos.setText(String.valueOf(diferenciamin));
                    tvtotal.setText(String.valueOf((int)total));
                    tvpatente.setText(vehiculo.getPatente());
                    tvfechaentrada.setText(vehiculo.getFechaentrada()+" "+vehiculo.getHoraentrada());
                    tvsalida.setText(salida);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}