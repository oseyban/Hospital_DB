package Hospital_Project;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static Hospital_Project.DataBankService.con;
import static Hospital_Project.HospitalService.*;

public class PatientService implements Methods{
  private static final LinkedList<Patient> patientList = new LinkedList<>();
  private static final LinkedList<Case> patientCaseList = new LinkedList<>();
  private final AppointmentService appointmentService = new AppointmentService();

    public void entryMenu() throws InterruptedException, IOException, SQLException {

        int secim = -1;
        do {
            System.out.println("=========================================");
            System.out.println("""
                    LUTFEN YAPMAK ISTEDIGINIZ ISLEMI SECINIZ:
                    \t=> 1-DOKTORLARI LİSTELE
                    \t=> 2-DOKTOR BUL
                    \t=> 3-DURUMUNU OGREN
                    \t=> 4-RANDEVU AL
                    \t=> 0-ANA MENU""");
            System.out.println("=========================================");
            try {
                secim = scan.nextInt();
                scan.nextLine();
            }
            catch (Exception e) {
                System.out.println("\"LUTFEN SIZE SUNULAN SECENEKLERIN DISINDA VERI GIRISI YAPMAYINIZ!\"");
                continue;
            }
            switch (secim) {
                case 1:
                    doctorService.list();
                    break;
                case 2:
                    doctorService.findDoctorByTitle();
                    // Thread.sleep(3000);
                    break;
                case 3:
                    System.out.println("DURUMUNUZU ÖĞRENMEK İÇİN HASTALIĞINIZI GİRİNİZ...");
                    String durum = scan.nextLine().trim();
                    System.out.println(findPatientCase(durum));
                    break;
                case 4:
                    appointmentService.haftalikRandevuTableList(appointmentService.haftalikRandevuTable());
                    break;
                case 0:
                    slowPrint("ANA MENUYE YÖNLENDİRİLİYORSUNUZ...\n", 20);
                    hospitalService.start();
                    break;
                default:
                    System.out.println("HATALI GİRİŞ, TEKRAR DENEYİNİZ...\n");
            }
        } while (secim != 0);
    }

    @Override
    public void add() {
        System.out.println("Eklemek istediğiniz hastanın ADINI giriniz:");
        String hastaAdi = scan.nextLine();

        System.out.println("Eklemek istediğiniz hastanın SOYADINI giriniz:");
        String hastaSoyadi = scan.nextLine();

        String durum;
        byte emergency;
        boolean aciliyet = false;

        List<String> hastaliklar = new ArrayList<>();

        try {
            String getDiseasesQuery = "SELECT disease FROM diseases";
            PreparedStatement prst = con.prepareStatement(getDiseasesQuery);
            ResultSet rs = prst.executeQuery();

            while (rs.next()) {
                hastaliklar.add(rs.getString("disease"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Hastalık isimleri alınırken bir hata oluştu.", e);
        }

            System.out.println("Hastanın Durumunu Girin:");
            for (String hastalik : hastaliklar) {
                System.out.println("\t=> " + hastalik);
            }
        durum = scan.nextLine();


        do {
            System.out.println("Eklemek istediğiniz hastanın aciliyetini belirtin:\n => Acil için: 1\n => Acil Değilse: 0\n");
            emergency = scan.nextByte();
            if (emergency == 1) {
                aciliyet = true;
            } else if (emergency == 0) {
                aciliyet = false;
            } else {
                System.out.println("Yanlış Aciliyet seçeneği girdiniz...");
                scan.next();
            }
        } while (emergency != 0 && emergency != 1);

        // Hasta bilgilerini veritabanına ekleyin
        try {
            String addPatientQuery = "INSERT INTO patients (patient_name, patient_surname, patient_case, isemergency) VALUES (?, ?, ?, ?)";
            PreparedStatement prst = con.prepareStatement(addPatientQuery);
            prst.setString(1, hastaAdi);
            prst.setString(2, hastaSoyadi);
            prst.setString(3, durum);
            prst.setBoolean(4, aciliyet);
            prst.executeUpdate();

            System.out.println(hastaAdi + " " + hastaSoyadi + " isimli hasta sisteme başarıyla eklenmiştir...");
            list();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        list();

        boolean isDeleted = false;
        int patientId = 0;

        while (true) {
            System.out.println("Silmek istediğiniz hastanın ID'sini giriniz:");
            try {
                patientId = scan.nextInt();
                scan.nextLine();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Geçersiz ID. Lütfen bir tam sayı girin.");
                scan.nextLine();
            }
        }

        try {
            String deletePatientQuery = "DELETE FROM patients WHERE patient_id = ?";
            PreparedStatement prst = con.prepareStatement(deletePatientQuery);
            prst.setInt(1, patientId);
            int affectedRows = prst.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Hasta başarıyla silindi.");
                isDeleted = true;
            } else {
                System.out.println("Silmek istediğiniz hasta bulunamadı.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Hasta silinirken bir hata oluştu.", e);
        }


        if (!isDeleted) {
            System.out.println("Silmek istediğiniz hasta listede bulunamadı.");
        }
    }



    @Override
    public void list() {
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("----------------------- HASTANEDE BULUNAN HASTALARIMIZ --------------------");
        System.out.printf("%-10s | %-10s | %-15s | %-20s | %-10s\n", "HASTA ID", "HASTA ISIM", "HASTA SOYISIM", "HASTA DURUM", "ACILIYET");
        System.out.println("---------------------------------------------------------------------------");

        try {
            String listPatientsQuery = "SELECT * FROM patients";
            PreparedStatement prst = con.prepareStatement(listPatientsQuery);
            ResultSet rs = prst.executeQuery();

            while (rs.next()) {
                int hastaID = rs.getInt("patient_id");
                String hastaAdi = rs.getString("patient_name");
                String hastaSoyadi = rs.getString("patient_surname");
                String hastaDurumu = rs.getString("patient_case");
                boolean aciliyet = rs.getBoolean("isemergency");

                System.out.printf("%-10s | %-10s | %-15s | %-20s | %-10s\n", hastaID, hastaAdi, hastaSoyadi, hastaDurumu, aciliyet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Hastalar listelenirken bir hata oluştu.", e);
        }

        System.out.println("---------------------------------------------------------------------------");
    }


    public void listPatientByCase(String aktuelDurum) {
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("-------- " + aktuelDurum.toUpperCase() + " HASTALARIMIZ ---------");
        System.out.printf("%-10s | %-10s | %-15s | %-20s | %-10s\n", "HASTA ID", "HASTA ISIM", "HASTA SOYISIM", "HASTA DURUM", "ACILIYET");
        System.out.println("---------------------------------------------------------------------------");

        try {
            String listPatientsByCaseQuery = "SELECT * FROM patients WHERE patient_case = ?";
            PreparedStatement prst = con.prepareStatement(listPatientsByCaseQuery);
            prst.setString(1, aktuelDurum);
            ResultSet rs = prst.executeQuery();

            while (rs.next()) {
                int hastaID = rs.getInt("patient_id");
                String hastaAdi = rs.getString("patient_name");
                String hastaSoyadi = rs.getString("patient_surname");
                String hastaDurumu = rs.getString("patient_case");
                boolean aciliyet = rs.getBoolean("isemergency");

                System.out.printf("%-10s | %-10s | %-15s | %-20s | %-10s\n", hastaID, hastaAdi, hastaSoyadi, hastaDurumu, aciliyet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Hastalar belirli bir duruma göre listelenirken bir hata oluştu.", e);
        }

        System.out.println("---------------------------------------------------------------------------");
    }

    public Patient findPatient(String aktuelDurum) {
        Patient patient = new Patient();
        for (int i = 0; i < hospital.hastaIsimleri.size(); i++) {

            if (aktuelDurum.equalsIgnoreCase(hospital.durumlar.get(i))) {

                patient.setIsim(hospital.hastaIsimleri.get(i));
                patient.setSoyIsim(hospital.hastaSoyIsimleri.get(i));
                patient.setHastaID(hospital.hastaIDleri.get(i));
                patient.setHastaDurumu(findPatientCase(aktuelDurum));
            }
        }
        return patient;
    }

    public Case findPatientCase(String aktuelDurum) {
        Case hastaDurumu = new Case("Yanlis Durum", false);
        switch (aktuelDurum.toLowerCase()) {
            case "allerji":
            case "bas agrisi":
            case "diabet":
            case "soguk alginligi":
                hastaDurumu.setEmergency(false);
                hastaDurumu.setActualCase(aktuelDurum);
                break;
            case "migren":
            case "kalp hastaliklari":
                hastaDurumu.setEmergency(true);
                hastaDurumu.setActualCase(aktuelDurum);

                break;
            default:
                System.out.println("Gecerli bir durum degil");

        }

        return hastaDurumu;
    }

    @Override
    public void createList() {
        for (String w : hospital.durumlar) {
            patientList.add(findPatient(w));
            patientCaseList.add(findPatientCase(w.toLowerCase()));
        }
    }
}
