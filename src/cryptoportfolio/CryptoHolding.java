/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoportfolio;

import java.io.Serializable;

/**
 *
 * @author Ba Thanh Danh Phan
 */
// CryptoHolding class
class CryptoHolding implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    double amount;
    double priceUSD;
    double change24h;

    public CryptoHolding(String name, double amount) {
        this.name = name;
        this.amount = amount;
        this.priceUSD = 0.0;
        this.change24h = 0.0;
    }
}