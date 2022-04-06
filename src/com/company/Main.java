package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.Date;

import static com.company.MysqlProperties.*;

public class Main extends JFrame {

    public static String[] naglowki = null;
    public static int wiersze = 0;
    public static int wierszeTMP = 0;
    public static int kolumny = 15;
    public static Object[][] dane;
    public static Object[][] daneTMP = new Object[25][kolumny];
    public static int liczba_duplikatow = 0;
    public static boolean istnieje_duplikat = false;

    /*public static JTable table = new JTable();
    public static Main okienko = new Main();
    public static JScrollPane scrollPane = new JScrollPane();*/

    public Main() {
        setSize(1500, 700);
        setTitle("Integracja systemów Lab4 - Piotr Błażewicz");
    }

    public static void importuj() {
        FileReader fr = null;
        FileReader frTMP = null;
        String linia = "";
        String liniaTMP = "";

        try {
            final String fileName = "src/com/company/katalog.txt";
            fr = new FileReader(fileName);
            frTMP = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Blad przy otwieraniu pliku!");
        }

        BufferedReader br = new BufferedReader(fr);
        BufferedReader brTMP = new BufferedReader(frTMP);
        try {
            int j = 0;

            wierszeTMP = wiersze;
            wiersze = 0;
            while (null != (liniaTMP = brTMP.readLine())) {
                wiersze++;
            }

            dane = new Object[wiersze][kolumny];

            while (null != (linia = br.readLine())) {
                String[] words = linia.split(";", -1);

                int i = 0;
                for (String word : words) {
                    if (word.isEmpty() && i < words.length - 1)
                        dane[j][i] = "Brak informacji";
                    else if (!word.isEmpty())
                        dane[j][i] = word;

                    i++;
                }
                j++;
            }
        } catch (IOException e) {
            System.out.println("Blad odczytu pliku!");
        }

        try {
            fr.close();
        } catch (IOException e) {
            System.out.println("Blad przy zamykaniu pliku!");
        }
    }

    //Polaczenie z baza danych na localhoscie
    public static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION.getValue(), DB_USER.getValue(), DB_PASSWORD.getValue());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return dbConnection;
    }

    public static void main(String[] args) throws FileNotFoundException {
        naglowki = new String[]{"Producent",
                "Przekątna",
                "Rozdzielczość",
                "Rodzaj powierzchni ekranu",
                "Ekran dotykowy",
                "Procesor",
                "Liczba rdzeni",
                "Prędkość taktowania MHz",
                "RAM",
                "pojemność dysku",
                "rodzaj dysku",
                "Układ graficzny",
                "Pamięć GPU",
                "System operacyjny",
                "Napęd fizyczny"};

        dane = new Object[wiersze][kolumny];

        Main okienko = new Main();
        okienko.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton button_eksport = new JButton("Eksportuj dane do txt");   //przycisk eksport
        button_eksport.setBounds(10, 470, 150, 50);
        button_eksport.setBackground(Color.ORANGE);
        button_eksport.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton button_import = new JButton("Importuj dane z txt");   //przycisk importu
        button_import.setBounds(170, 470, 150, 50);
        button_import.setBackground(Color.YELLOW);
        button_import.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton button_eksport_xml = new JButton("Eksportuj dane do xml");   //przycisk eksport xml
        button_eksport_xml.setBounds(330, 470, 150, 50);
        button_eksport_xml.setBackground(Color.CYAN);
        button_eksport_xml.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton button_import_xml = new JButton("Importuj dane z xml");   //przycisk importu xml
        button_import_xml.setBounds(490, 470, 150, 50);
        button_import_xml.setBackground(Color.GREEN);
        button_import_xml.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton button_eksport_DB = new JButton("Eksportuj dane do bazy danych");   //przycisk eksport do BD
        button_eksport_DB.setBounds(650, 470, 180, 50);
        button_eksport_DB.setBackground(Color.PINK);
        button_eksport_DB.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton button_import_DB = new JButton("Importuj dane z bazy danych");   //przycisk importu z BD
        button_import_DB.setBounds(840, 470, 180, 50);
        button_import_DB.setBackground(Color.RED);
        button_import_DB.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JTextArea infoTA = new JTextArea();
        infoTA.setBounds(10, 430, 300, 20);
        infoTA.setText("Witam serdecznie!");
        infoTA.setFocusable(false);

        DefaultTableModel tableModel = new DefaultTableModel(0, 0);
        tableModel.setColumnIdentifiers(naglowki);

        JTable table = new JTable(tableModel) {
            @Override
            public void setValueAt(Object aValue, int row, int column) {    //edycja danych
                if (aValue.toString().trim().isEmpty()) {   //trim usuniecie bialych znakow - zeby spacje uznawalo jako puste
                    JOptionPane.showMessageDialog(okienko, "Pole nie może być puste!");
                    //System.out.println("Puste pole!");
                } else if ((column == 4 || column == 10) && aValue.toString().trim().length() != 3) {
                    JOptionPane.showMessageDialog(okienko, "Tekst musi miec 3 znaki!");
                } else if (column == 1 && !aValue.toString().endsWith("\"")) {
                    JOptionPane.showMessageDialog(okienko, "Pole musi się kończyć na \"");
                } else if (column == 2 & !aValue.toString().matches("[0-9]+x[0-9]+")) {
                    JOptionPane.showMessageDialog(okienko, "Wprowadź wartość według wzoru, np. 1920x1080");
                } else {
                    super.setValueAt(aValue, row, column);
                    dane[row][column] = aValue;
                }
            }
        };

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 40, 200, 300);

        okienko.add(button_eksport);
        okienko.add(button_import);
        okienko.add(button_eksport_xml);
        okienko.add(button_import_xml);
        okienko.add(button_eksport_DB);
        okienko.add(button_import_DB);
        okienko.add(infoTA);
        okienko.add(scrollPane);
        okienko.setVisible(true);

        button_eksport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrintWriter zapis = null;
                try {
                    zapis = new PrintWriter("wynik.txt");
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }

                for (int i = 0; i < wiersze; i++) {
                    for (int j = 0; j < 15; j++) {
                        zapis.print(dane[i][j] + ";");
                    }
                    if (i < wiersze - 1)
                        zapis.print("\n");
                }
                zapis.close();
                infoTA.setText("Zapisano dane do pliku txt");
            }
        });

        button_import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importuj();

                for (int i = wierszeTMP - 1; i >= 0; i--) {
                    tableModel.removeRow(i);
                }

                for (int i = 0; i < wiersze; i++) {
                    tableModel.addRow(dane[i]);
                }

                infoTA.setText("Wczytano dane z pliku txt");
                okienko.repaint();
            }
        });

        button_eksport_xml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DocumentBuilder builder;
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    builder = factory.newDocumentBuilder();
                    Document document = builder.newDocument();

                    Element laptops = document.createElement("laptops");
                    laptops.setAttribute("moddate", String.valueOf(new Date()));

                    int j;
                    for (int i = 0; i < wiersze; i++) {
                        j = 0;
                        Element laptop = document.createElement("laptop");
                        laptop.setAttribute("id", String.valueOf(i + 1));

                        //PRODUCENT
                        Element manufacturer = document.createElement("manufacturer");
                        manufacturer.setTextContent((String) dane[i][j]);
                        j++;

                        //EKRAN
                        Element screen = document.createElement("screen");

                        Element size = document.createElement("size");
                        size.setTextContent((String) dane[i][j]);
                        j++;

                        Element resolution = document.createElement("resolution");
                        resolution.setTextContent((String) dane[i][j]);
                        j++;

                        Element type = document.createElement("type");
                        type.setTextContent((String) dane[i][j]);
                        j++;

                        screen.setAttribute("touch", (String) dane[i][j]);
                        j++;

                        //PROCESOR
                        Element processor = document.createElement("processor");

                        Element name = document.createElement("name");
                        name.setTextContent((String) dane[i][j]);
                        j++;

                        Element physical_cores = document.createElement("physical_cores");
                        physical_cores.setTextContent((String) dane[i][j]);
                        j++;

                        Element clock_speed = document.createElement("clock_speed");
                        clock_speed.setTextContent((String) dane[i][j]);
                        j++;

                        //RAM
                        Element ram = document.createElement("ram");
                        ram.setTextContent((String) dane[i][j]);
                        j++;

                        //DYSK
                        Element disc = document.createElement("disc");

                        Element storage = document.createElement("storage");
                        storage.setTextContent((String) dane[i][j]);
                        j++;

                        disc.setAttribute("type", (String) dane[i][j]);
                        j++;

                        //GPU
                        Element graphic_card = document.createElement("graphic_card");

                        Element nameGPU = document.createElement("name");
                        nameGPU.setTextContent((String) dane[i][j]);
                        j++;

                        Element memory = document.createElement("memory");
                        memory.setTextContent((String) dane[i][j]);
                        j++;

                        //SYSTEM OPERACYJNY
                        Element os = document.createElement("os");
                        os.setTextContent((String) dane[i][j]);
                        j++;

                        //NAPED
                        Element disc_reader = document.createElement("disc_reader");
                        disc_reader.setTextContent((String) dane[i][j]);

                        laptop.appendChild(manufacturer);

                        screen.appendChild(size);
                        screen.appendChild(resolution);
                        screen.appendChild(type);
                        laptop.appendChild(screen);

                        processor.appendChild(name);
                        processor.appendChild(physical_cores);
                        processor.appendChild(clock_speed);
                        laptop.appendChild(processor);

                        laptop.appendChild(ram);

                        disc.appendChild(storage);
                        laptop.appendChild(disc);

                        graphic_card.appendChild(nameGPU);
                        graphic_card.appendChild(memory);
                        laptop.appendChild(graphic_card);

                        laptop.appendChild(os);

                        laptop.appendChild(disc_reader);

                        laptops.appendChild(laptop);
                    }

                    document.appendChild(laptops);

                    Transformer t = TransformerFactory.newInstance().newTransformer();
                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                    t.setOutputProperty(OutputKeys.METHOD, "xml");
                    t.transform(new DOMSource(document), new StreamResult(new FileOutputStream("laptopy.xml")));
                } catch (ParserConfigurationException | TransformerConfigurationException | FileNotFoundException parserConfigurationException) {
                    parserConfigurationException.printStackTrace();
                } catch (TransformerException transformerException) {
                    transformerException.printStackTrace();
                }
                infoTA.setText("Zapisano dane do pliku xml");
            }
        });

        button_import_xml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("laptopy.xml");
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(file);
                    document.getDocumentElement().normalize();
                    //System.out.println("Root element: " + document.getDocumentElement().getNodeName());
                    NodeList nodeList = document.getElementsByTagName("laptop");

                    int j = 0;
                    wierszeTMP = wiersze;
                    wiersze = nodeList.getLength();
                    dane = new Object[wiersze][kolumny];

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        //System.out.println("\nLaptop id: "+ node.getAttributes().getNamedItem("id").getNodeValue());

                        j = 0;
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;

                            dane[i][j] = element.getElementsByTagName("manufacturer").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("size").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("resolution").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("type").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("screen").item(0).getAttributes().getNamedItem("touch").getNodeValue();
                            j++;
                            dane[i][j] = element.getElementsByTagName("name").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("physical_cores").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("clock_speed").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("ram").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("storage").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("disc").item(0).getAttributes().getNamedItem("type").getNodeValue();
                            j++;
                            dane[i][j] = element.getElementsByTagName("name").item(1).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("memory").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("os").item(0).getTextContent();
                            j++;
                            dane[i][j] = element.getElementsByTagName("disc_reader").item(0).getTextContent();
                        }
                    }

                    for (int i = wierszeTMP - 1; i >= 0; i--) {
                        tableModel.removeRow(i);
                    }

                    for (int i = 0; i < wiersze; i++) {
                        tableModel.addRow(dane[i]);
                    }

                } catch (ParserConfigurationException parserConfigurationException) {
                    parserConfigurationException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (SAXException saxException) {
                    saxException.printStackTrace();
                }
                infoTA.setText("Wczytano dane z pliku xml");
                okienko.repaint();
            }
        });

        Connection connection = getDBConnection();
        button_eksport_DB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM laptop");
                    liczba_duplikatow = 0;

                    int i = 0, j = 0;
                    while (resultSet.next()) {
                        j = 0;
                        daneTMP[i][j] = resultSet.getString("producent");
                        j++;
                        daneTMP[i][j] = resultSet.getString("przekatna");
                        j++;
                        daneTMP[i][j] = resultSet.getString("rozdzielczosc");
                        j++;
                        daneTMP[i][j] = resultSet.getString("rodzajPowierzchni");
                        j++;
                        daneTMP[i][j] = resultSet.getString("ekranDotykowy");
                        j++;
                        daneTMP[i][j] = resultSet.getString("procesor");
                        j++;
                        daneTMP[i][j] = resultSet.getString("liczbaRdzeni");
                        j++;
                        daneTMP[i][j] = resultSet.getString("predkoscTaktowania");
                        j++;
                        daneTMP[i][j] = resultSet.getString("ram");
                        j++;
                        daneTMP[i][j] = resultSet.getString("pojemnoscDysku");
                        j++;
                        daneTMP[i][j] = resultSet.getString("rodzajDysku");
                        j++;
                        daneTMP[i][j] = resultSet.getString("ukladGraficzny");
                        j++;
                        daneTMP[i][j] = resultSet.getString("pamiecGpu");
                        j++;
                        daneTMP[i][j] = resultSet.getString("systemOperacyjny");
                        j++;
                        daneTMP[i][j] = resultSet.getString("napedFizyczny");
                        i++;
                    }

                    for (i = 0; i < wiersze; i++) {
                        istnieje_duplikat = false;
                        for (j = 0; j < wiersze; j++) {
                            if (String.valueOf(dane[i][0]).equals(String.valueOf(daneTMP[j][0])) && String.valueOf(dane[i][1]).equals(String.valueOf(daneTMP[j][1])) && String.valueOf(dane[i][2]).equals(String.valueOf(daneTMP[j][2])) && String.valueOf(dane[i][3]).equals(String.valueOf(daneTMP[j][3])) && String.valueOf(dane[i][4]).equals(String.valueOf(daneTMP[j][4])) && String.valueOf(dane[i][5]).equals(String.valueOf(daneTMP[j][5])) && String.valueOf(dane[i][6]).equals(String.valueOf(daneTMP[j][6])) && String.valueOf(dane[i][7]).equals(String.valueOf(daneTMP[j][7])) && String.valueOf(dane[i][8]).equals(String.valueOf(daneTMP[j][8])) && String.valueOf(dane[i][9]).equals(String.valueOf(daneTMP[j][9])) && String.valueOf(dane[i][10]).equals(String.valueOf(daneTMP[j][10])) && String.valueOf(dane[i][11]).equals(String.valueOf(daneTMP[j][11])) && String.valueOf(dane[i][12]).equals(String.valueOf(daneTMP[j][12])) && String.valueOf(dane[i][13]).equals(String.valueOf(daneTMP[j][13])) && String.valueOf(dane[i][14]).equals(String.valueOf(daneTMP[j][14]))) {
                                istnieje_duplikat = true;
                            }
                        }

                        if (istnieje_duplikat) {
                            liczba_duplikatow++;
                        } else {
                            connection.createStatement().execute("INSERT INTO laptop VALUES (NULL, '" + dane[i][0] + "','" + dane[i][1] + "','" + dane[i][2] + "','" + dane[i][3] + "','" + dane[i][4] + "','" + dane[i][5] + "','" + dane[i][6] + "','" + dane[i][7] + "','" + dane[i][8] + "','" + dane[i][9] + "','" + dane[i][10] + "','" + dane[i][11] + "','" + dane[i][12] + "','" + dane[i][13] + "','" + dane[i][14] + "')");
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                infoTA.setText("Zapisano dane do bazy danych! Liczba duplikatow: " + liczba_duplikatow);
            }
        });

        button_import_DB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //ResultSet resultSet = statement.executeQuery("SELECT idLaptop, producent, przekatna, rozdzielczosc, rodzajPowierzchni, ekranDotykowy, procesor, liczbaRdzeni, procesor, liczbaRdzeni, predkoscTaktowania, ram, pojemnoscDysku, rodzajDysku, ukladGraficzny, pamiecGpu, systemOperacyjny, napedFizyczny FROM laptop");
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM laptop");
                    ResultSet resultSetTMP = connection.createStatement().executeQuery("SELECT * FROM laptop");

                    wierszeTMP = wiersze;
                    wiersze = 0;
                    while (resultSetTMP.next()) {
                        wiersze++;
                    }

                    dane = new Object[wiersze][kolumny];

                    int i = 0, j = 0;
                    while (resultSet.next()) {
                        j = 0;
                        dane[i][j] = resultSet.getString("producent");
                        j++;
                        dane[i][j] = resultSet.getString("przekatna");
                        j++;
                        dane[i][j] = resultSet.getString("rozdzielczosc");
                        j++;
                        dane[i][j] = resultSet.getString("rodzajPowierzchni");
                        j++;
                        dane[i][j] = resultSet.getString("ekranDotykowy");
                        j++;
                        dane[i][j] = resultSet.getString("procesor");
                        j++;
                        dane[i][j] = resultSet.getString("liczbaRdzeni");
                        j++;
                        dane[i][j] = resultSet.getString("predkoscTaktowania");
                        j++;
                        dane[i][j] = resultSet.getString("ram");
                        j++;
                        dane[i][j] = resultSet.getString("pojemnoscDysku");
                        j++;
                        dane[i][j] = resultSet.getString("rodzajDysku");
                        j++;
                        dane[i][j] = resultSet.getString("ukladGraficzny");
                        j++;
                        dane[i][j] = resultSet.getString("pamiecGpu");
                        j++;
                        dane[i][j] = resultSet.getString("systemOperacyjny");
                        j++;
                        dane[i][j] = resultSet.getString("napedFizyczny");
                        i++;
                    }

                    for (i = wierszeTMP - 1; i >= 0; i--) {
                        tableModel.removeRow(i);
                    }

                    for (i = 0; i < wiersze; i++) {
                        tableModel.addRow(dane[i]);
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                infoTA.setText("Wczytano dane z bazy danych");
                okienko.repaint();
            }
        });
    }
}
