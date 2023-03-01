package appnet.tech.parkingofappnet;

public class Utilidades {

    public static final String TABLA_VEHICULOS="vehiculos";
    public static final String CAMPO_PATENTE="patente";
    public static final String CAMPO_FECHAENTRADA="fechaentrada";
    public static final String CAMPO_HORAENTRADA="horaentrada";
    public static final String CAMPO_HORASALIDA="horasalida";
    public static final String CAMPO_FECHASALIDA="fechasalida";

    public static final String CREAR_TABLA_VEHICULOS="create table "+TABLA_VEHICULOS+
            " ("
            +CAMPO_PATENTE+" text,"
            +CAMPO_FECHAENTRADA+" text,"
            +CAMPO_FECHASALIDA+" text,"
            +CAMPO_HORAENTRADA+" text,"
            +CAMPO_HORASALIDA+" text)";

    public static final String TABLA_CONFIGURACION="configuracion";

    public static final String CAMPO_TOKENCONF="token";
    public static final String CAMPO_RUTEMPRESACONF="rutemp";
    public static final String CAMPO_RAZONSOCIALCONF="razonsocial";
    public static final String CAMPO_BODEGACONF="bodega";
    public static final String CAMPO_GIROCONF="giro";
    public static final String CAMPO_LOGOCONF="logo";
    public static final String CAMPO_VOUCHERCONF="cantidad";
    public static final String CAMPO_CENTROCONF="centro";
    public static final String CAMPO_DOCCONF="doc";
    public static final String CAMPO_DIRECCIONCONF="direccion";
    public static final String CAMPO_ESTADOCONF="estado";
    public static final String CAMPO_UNIDADSIICONF="unidad";
    public static final String CAMPO_RESOLUCIONSIICONF ="resolucion";
    public static final String CAMPO_CATCONF ="categoria";
    public static final String CAMPO_LISTAPRECIOCONF ="listaprecioconf";
    public static final String CAMPO_TOKENMACHCONF ="tokenmach";
    public static final String CAMPO_NOMBREUSERCONF ="nombreuser";

    public static final String CREAR_TABLA_CONFIGURACION="create table "+TABLA_CONFIGURACION+
            " ("
            +CAMPO_TOKENCONF+" text,"
            +CAMPO_RAZONSOCIALCONF+" text,"
            +CAMPO_RUTEMPRESACONF+" text,"
            +CAMPO_BODEGACONF+" text,"
            +CAMPO_LOGOCONF+" text,"
            +CAMPO_VOUCHERCONF+" text,"
            +CAMPO_CENTROCONF+" text,"
            +CAMPO_DOCCONF+" text,"
            +CAMPO_ESTADOCONF+" text,"
            +CAMPO_CATCONF+" text,"
            +CAMPO_DIRECCIONCONF+" text,"
            +CAMPO_UNIDADSIICONF+" text,"
            +CAMPO_LISTAPRECIOCONF+" text,"
            +CAMPO_RESOLUCIONSIICONF +" text,"
            +CAMPO_NOMBREUSERCONF +" text,"
            +CAMPO_TOKENMACHCONF +" text,"
            +CAMPO_GIROCONF+" text)";

    public static final String TABLA_TIPOPAGO="tipopago";

    public static final String CAMPO_IDTIPO="id";
    public static final String CAMPO_NOMBRETIPO="nombre";
    public static final String CAMPO_EFECTIVOTIPO="efectivo";

    public static final String CREAR_TABLA_TIPOPAGO="create table "+TABLA_TIPOPAGO+
            " ("
            +CAMPO_IDTIPO+" text,"
            +CAMPO_EFECTIVOTIPO+" text,"
            +CAMPO_NOMBRETIPO+" text)";

    public static final String TABLA_PARKING="parking";

    public static final String CAMPO_TIPOCOBROPARKING="tipocobro";
    public static final String CAMPO_VALORCOBROPARKING="valor";
    public static final String CAMPO_COBROBASEPARKING="cobrobase";
    public static final String CAMPO_MINCOBROBASEPARKING="mincobrobase";
    public static final String CAMPO_ESPACIOSPARKING="espacios";

    public static final String CREAR_TABLA_PARKING="create table "+TABLA_PARKING+
            " ("
            +CAMPO_TIPOCOBROPARKING+" text,"
            +CAMPO_VALORCOBROPARKING+" text,"
            +CAMPO_COBROBASEPARKING+" text,"
            +CAMPO_MINCOBROBASEPARKING+" text,"
            +CAMPO_ESPACIOSPARKING+" text)";

    public static final String URL_IP="https://appnetstore.cl/";
    public static final String URL_MACHCREARPAGO="https://biz-sandbox.soymach.com/payments";
    public static final String URL_MACHCCHECKEAR="https://biz-sandbox.soymach.com/payments/";
    public static final String URL_MACHCANCELAR="https://biz-sandbox.soymach.com/payments/";

    public static final String TOKEN_MACH="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJidXNpbmVzc01hY2hJZCI6IjAyNzg1ZTY3LWQ3ODQtNGQ0ZS1hOTZiLWI1MWU2YWEwMGYyYyIsImJ1c2luZXNzU2VjcmV0SWQiOiI5Zjg1ODkyMS05MmY4LTQ0MjgtYWIyMS01MmQ1N2YyMzQ4MjciLCJzY29wZXMiOlsicGF5bWVudHMuY3JlYXRlIiwicGF5bWVudHMuZ2V0Il0sImlhdCI6MTU5Njk5MDU3N30.u4u45UV0u1E_NCsBLCpYW5P5HFr6Qhwgbo-ZpBEdu3M";

    public static final String URL_LOGIN=URL_IP+"api/login";
    public static final String URL_SUCURSAL=URL_IP+"api/sucursal";
    public static final String URL_TIPOPAGO=URL_IP+"api/formapago";
    public static final String URL_REGISTRO=URL_IP+"api/parking/dia";
    public static final String URL_EMPRESA=URL_IP+"api/empresa";
    public static final String URL_VOUCHER=URL_IP+"api/parking/venta";
    public static final String URL_CONFIGPARKING=URL_IP+"api/parking/parametros";
    public static final String URL_CIERRECAJA=URL_IP+"api/informe/caja/normal";
    public static final String URL_ENTRADA=URL_IP+"api/parking/arrive";
}
