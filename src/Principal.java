import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.Gson;
import java.util.Set;
import java.util.Currency;


public class Principal {

    public static String divisaInicial;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double cantidadDivisa;
        String divisaAConvertir = "";
        Gson gson = new Gson();

        Set<Currency> listaDeDivisas = Currency.getAvailableCurrencies();

        outerLoop:
        while (true) {
            System.out.println("""
                    *************************************************
                    ¡Bienvenido al Conversor de moneda !
                    ...
                    Ingrese el código de divisa de origen:""");

            divisaInicial = obtenerCodigoDivisa(scanner, listaDeDivisas);
            if (divisaInicial.equalsIgnoreCase("Salir")) break outerLoop;

            System.out.println("Ingrese el codigo de divisa destino");
            divisaAConvertir = obtenerCodigoDivisa(scanner, listaDeDivisas);
            if (divisaAConvertir.equalsIgnoreCase("Salir")) break outerLoop;

            cantidadDivisa = obtenerCantidad(scanner);
            if (cantidadDivisa == -1) break outerLoop;

            realizarConversion(scanner, gson, divisaInicial, divisaAConvertir, cantidadDivisa);
        }
    }

    private static String obtenerCodigoDivisa(Scanner scanner, Set<Currency> listaDeDivisas) {
        while (true) {
            String divisa = scanner.nextLine().toUpperCase();
            if (divisa.equalsIgnoreCase("Salir")) return divisa;
            if (listaDeDivisas.stream().anyMatch(currency -> currency.getCurrencyCode().equals(divisa))) {
                return divisa;
            } else {
                System.out.println("Ingrese un código de divisa válido:");
            }
        }
    }

    private static double obtenerCantidad(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Ingrese la cantidad de " + divisaInicial + " que desea convertir:");
                String cantidadUsuario = scanner.nextLine();
                if (cantidadUsuario.equalsIgnoreCase("Salir")) return -1;

                double cantidadDivisa = Double.parseDouble(cantidadUsuario);
                if (cantidadDivisa <= 0) {
                    System.out.println("La cantidad debe ser mayor que cero.");
                } else {
                    return cantidadDivisa;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    private static void realizarConversion(Scanner scanner, Gson gson, String divisaInicial, String divisaAConvertir, double cantidadDivisa) {
        ConsultaDivisa api = new ConsultaDivisa();
        String resultado = api.consultaDivisa();

        try {
            Divisa divisa = gson.fromJson(resultado, Divisa.class);
            double tipoDeCambio = divisa.conversion_rates().get(divisaAConvertir);
            double conversion = cantidadDivisa * tipoDeCambio;
            DecimalFormat formatoDecimal = new DecimalFormat("#,##0.00");
            String conversionDecimal = formatoDecimal.format(conversion);

            mostrarResultado(conversionDecimal, cantidadDivisa, divisaInicial, tipoDeCambio, divisaAConvertir);
            registrarTransaccion(cantidadDivisa, divisaInicial, tipoDeCambio, conversionDecimal, divisaAConvertir);

            if (!realizarOtraOperacion(scanner)) {
                System.out.println("Programa finalizado, gracias! ");
            }

        } catch (Exception e) {
            System.out.println("Error en la conversión: " + e.getMessage());
        }
    }

    private static void mostrarResultado(String conversionDecimal, double cantidadDivisa, String divisaInicial, double tipoDeCambio, String divisaAConvertir) {
        System.out.println("""
                *************************************************
                
                Cantidad preliminar:  """ + " " +cantidadDivisa + " " + divisaInicial + "\n" + """ 
                Tipo de Cambio: """ + tipoDeCambio + " " + divisaAConvertir + "/" + divisaInicial + "\n" + """
                Resultado de operación: """ + conversionDecimal + " " + divisaAConvertir + """
                
                *************************************************
                """);
    }

    private static void registrarTransaccion(double cantidadDivisa, String divisaInicial, double tipoDeCambio, String conversionDecimal, String divisaAConvertir) {
        try (FileWriter fileWriter = new FileWriter("conversor-registro-transacciones.txt", true)) {
            Date fecha = new Date();
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            String fechaConFormato = formatoFecha.format(fecha);
            fileWriter.write("""
                    \nFecha de operación: """ + fechaConFormato + """
                    \nCantidad preliminar: """ + cantidadDivisa + " " + divisaInicial + """
                    \nTipo de Cambio: """ + tipoDeCambio + " " + divisaAConvertir + "/" + divisaInicial + """
                    \nResultado de operación: """ + conversionDecimal + " " + divisaAConvertir + """
                    \n**********
                    """);
        } catch (IOException e) {
            System.out.println("Error al registrar transacción: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private static boolean realizarOtraOperacion(Scanner scanner) {
        System.out.println("¿Desea hacer otra operación?");
        System.out.println("→ Si");
        System.out.println("→ No");

        while (true) {
            String respuesta = scanner.nextLine().toLowerCase();
            if (respuesta.equals("no") || respuesta.equals("salir")) {
                return false;
            } else if (respuesta.equals("si")) {
                return true;
            } else {
                System.out.println("Opción inválida, por favor ingrese 'si' o 'no'.");
            }
        }
    }
}

