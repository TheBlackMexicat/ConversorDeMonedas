import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class conversorDeMonedas {
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/48beb9a692e304ad03d4bebc/latest/";
    private static final String ARCHIVO_CONSULTAS = "consultas.json";

    public static void main(String[] args) {
        Scanner opcionUsuario = new Scanner(System.in);
        boolean salirahora = false;

        while (!salirahora) {
            System.out.println("¡Bienvenido!\n\n" +
                    "Aquí podrás conocer el tipo de cambio de pesos mexicanos a la moneda seleccionada.\n" +
                    "Ingresa el número de la opción que deseas seleccionar:\n\n" +
                    "1- Pesos mexicanos a Dólares americanos.\n" +
                    "2- Dólares a Pesos mexicanos.\n\n" +
                    "3- Pesos mexicanos a Euros.\n" +
                    "4- Euros a Pesos mexicanos.\n\n" +
                    "5- Pesos mexicanos a Pesos colombianos.\n" +
                    "6- Pesos colombianos a Pesos mexicanos.\n\n" +
                    "7- Pesos mexicanos a Pesos argentinos.\n" +
                    "8- Pesos argentinos a Pesos mexicanos.\n\n" +
                    "9- Pesos mexicanos a Yenes.\n" +
                    "10- Yenes a Pesos mexicanos\n\n" +
                    "0- Salir");

            int opcion = opcionUsuario.nextInt();

            if (opcion == 0) {
                salirahora = true;
                System.out.println("¡Gracias por usar el conversor de monedas!\nEspero te haya sido muy útil.");
                continue;
            }

            System.out.print("¿Cuál es la cantidad que desea convertir? ");
            double cantidad = opcionUsuario.nextDouble();

            String resultado = null;
            switch (opcion) {
                case 1:
                    resultado = convertirAPesos("MXN", "USD", cantidad);
                    break;
                case 2:
                    resultado = convertirDesdePesos("USD", "MXN", cantidad);
                    break;
                case 3:
                    resultado = convertirAPesos("MXN", "EUR", cantidad);
                    break;
                case 4:
                    resultado = convertirDesdePesos("EUR", "MXN", cantidad);
                    break;
                case 5:
                    resultado = convertirAPesos("MXN", "COP", cantidad);
                    break;
                case 6:
                    resultado = convertirDesdePesos("COP", "MXN", cantidad);
                    break;
                case 7:
                    resultado = convertirAPesos("MXN", "ARS", cantidad);
                    break;
                case 8:
                    resultado = convertirDesdePesos("ARS", "MXN", cantidad);
                    break;
                case 9:
                    resultado = convertirAPesos("MXN", "JPY", cantidad);
                    break;
                case 10:
                    resultado = convertirDesdePesos("JPY", "MXN", cantidad);
                    break;
                default:
                    System.out.println("La opción que ingresó no es válida. Por favor, intente con una opción " +
                            "que observe en el menú.");
            }

            if (resultado != null) {
                System.out.println(resultado);
                guardarConsulta(opcion, cantidad, resultado);
            }
        }

        opcionUsuario.close();
    }

    private static String convertirAPesos(String monedaOrigen, String divisaExtranjera, double cantidad) {
        String json = obtenerTipoCambio(monedaOrigen);
        double tipoCambio = analizarTipoCambio(json, divisaExtranjera);
        double cantidadConvertida = cantidad * tipoCambio;

        return String.format("El equivalente de %.2f %s es %.2f %s%n", cantidad, monedaOrigen, cantidadConvertida, divisaExtranjera);
    }

    private static String convertirDesdePesos(String divisaExtranjera, String monedaNacional, double cantidad) {
        String json = obtenerTipoCambio(monedaNacional);
        double tipoCambio = analizarTipoCambio(json, divisaExtranjera);
        double cantidadConvertida = cantidad / tipoCambio;

        return String.format("El equivalente de %.2f %s es %.2f %s%n", cantidad, divisaExtranjera, cantidadConvertida, monedaNacional);
    }

    private static String obtenerTipoCambio(String monedaNacional) {
        try {
            URL url = new URL(API_URL + monedaNacional);
            HttpURLConnection conneccion = (HttpURLConnection) url.openConnection();
            conneccion.setRequestMethod("GET");
            conneccion.setConnectTimeout(5000);
            conneccion.setReadTimeout(5000);

            int responseCode = conneccion.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader lector = new BufferedReader(new InputStreamReader(conneccion.getInputStream()));
                StringBuilder respuestaJson = new StringBuilder();
                String line;

                while ((line = lector.readLine()) != null) {
                    respuestaJson.append(line);
                }

                lector.close();
                return respuestaJson.toString();
            } else {
                System.out.println("Error: No se pudo obtener la tasa de cambio. Código de respuesta: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double analizarTipoCambio(String jsonRespuesta, String divisaExtranjera) {
        if (jsonRespuesta != null) {
            JsonObject jsonObject = JsonParser.parseString(jsonRespuesta).getAsJsonObject();
            return jsonObject.getAsJsonObject("conversion_rates").get(divisaExtranjera).getAsDouble();
        }
        return 0;
    }

    private static void guardarConsulta(int opcion, double cantidad, String resultado) {
        Map<String, Object> consulta = new HashMap<>();
        consulta.put("opcion", opcion);
        consulta.put("cantidad", cantidad);
        consulta.put("resultado", resultado);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter escritor = new FileWriter(ARCHIVO_CONSULTAS, true);
            gson.toJson(consulta, escritor);
            escritor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
