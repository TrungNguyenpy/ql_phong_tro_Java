package com.example.manager_room.test;

import org.junit.Assert;
import org.junit.Test;

public class InvoiceCalculatorTest {

    // Hàm tính tổng tiền hóa đơn
    public double calculateTotalInvoice(double roomPrice, int electricUsed, double electricRate, int waterUsed, double waterRate, double serviceFee) {
        return roomPrice + (electricUsed * electricRate) + (waterUsed * waterRate) + serviceFee;
    }

    @Test
    public void testCalculateTotalInvoice_CorrectCalculation() {
        double roomPrice = 3500000;
        int electricUsed = 55;   // 55 kWh * 3500 = 192.500
        double electricRate = 3500;
        int waterUsed = 12;      // 12 m3 * 15000 = 180.000
        double waterRate = 15000;
        double serviceFee = 100000;

        // Mong đợi: 3.500.000 + 192.500 + 180.000 + 100.000 = 3.972.500 VNĐ
        double expectedTotal = 3972500;
        double actualTotal = calculateTotalInvoice(roomPrice, electricUsed, electricRate, waterUsed, waterRate, serviceFee);

        Assert.assertEquals(expectedTotal, actualTotal, 0.001);
    }
}