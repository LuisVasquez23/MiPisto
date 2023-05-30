package sv.edu.catolica.lv.mipisto;

public class HistorialItem {
    private String date;
    private String data1;
    private String data2;

    public HistorialItem(String date, String data1, String data2) {
        this.date = date;
        this.data1 = data1;
        this.data2 = data2;
    }

    public String getDate() {
        return date;
    }

    public String getData1() {
        return data1;
    }

    public String getData2() {
        return data2;
    }
}
