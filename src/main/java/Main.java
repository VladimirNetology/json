import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== [CSV to JSON] ================");
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        System.out.println("[parseCSV]");
        list.forEach(System.out::println);
        String json = listToJson(list);
        System.out.println();
        System.out.println("[listToJson]");
        System.out.println(json);
        writeString(json);
        writeJsonFromList(list);

        System.out.println();
        System.out.println("=== [XML to Object] ================");
        List<Employee> list2 = parseXML("data.xml");
        System.out.println();
        System.out.println("[parseXML]");
        list2.forEach(System.out::println);

        System.out.println();
        System.out.println("=== [JSON to Object] ================");
        String json2 = readString("data.json");
        System.out.println("[readString]");
        System.out.println(json2);
        List<Employee> list3 = jsonToList(json2);
        System.out.println();
        System.out.println("[jsonToList]");
        list3.forEach(System.out::println);

    }

    private static void writeString(String text) {
        try (FileWriter writer = new FileWriter("data.json", false)) {
            writer.write(text);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String readString(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            String textFile = "";
            while ((s = br.readLine()) != null) {
                textFile += s.trim();
            }
            return textFile;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static List<Employee> jsonToList(String json) {
        Gson gson = new Gson();
        List<Employee> staff = gson.fromJson(json, new TypeToken<List<Employee>>() {}.getType());
        System.out.println(staff);
        return staff;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseXML(String file) {
        List<Employee> employees = new ArrayList();
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            Node root = document.getDocumentElement();
            NodeList books = root.getChildNodes();
            for (int i = 0; i < books.getLength(); i++) {
                Node book = books.item(i);
                long id = 0;
                String firstName = "";
                String lastName = "";
                String country = "";
                int age = 0;
                if (book.getNodeType() != Node.TEXT_NODE) {
                    NodeList bookProps = book.getChildNodes();
                    for (int j = 0; j < bookProps.getLength(); j++) {
                        Node bookProp = bookProps.item(j);
                        if (bookProp.getNodeType() != Node.TEXT_NODE) {
                            System.out.println(bookProp.getNodeName() + ":" + bookProp.getChildNodes().item(0).getTextContent());
                            switch (bookProp.getNodeName()) {
                                case "id" -> id = Long.parseLong(bookProp.getChildNodes().item(0).getTextContent());
                                case "firstName" -> firstName = bookProp.getChildNodes().item(0).getTextContent();
                                case "lastName" -> lastName = bookProp.getChildNodes().item(0).getTextContent();
                                case "country" -> country = bookProp.getChildNodes().item(0).getTextContent();
                                case "age" -> age = Integer.parseInt(bookProp.getChildNodes().item(0).getTextContent());
                            }
                        }
                    }
                    employees.add(new Employee(id, firstName, lastName, country, age));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.out);
        }
        return employees;
    }

    private static void writeJsonFromList(List<Employee> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("dataFromList.json")) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> staff = csv.parse();
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
