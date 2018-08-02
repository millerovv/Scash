package com.moneytracker.akscorp.moneytracker.model

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import com.moneytracker.akscorp.moneytracker.R
import com.moneytracker.akscorp.moneytracker.model.entities.Currency
import com.moneytracker.akscorp.moneytracker.model.entities.Money
import kotlinx.android.parcel.Parcelize
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


val defaultCurrency = Currency.EUR
var lastCurrencyUpdateTime = ""

/**
 * Main currency is defaultCurrency [Currency.EUR]
 */
class CurrencyConverter {

    /**
     * Convert [money] to another [currency]
     */
    private fun fromDefaultCurrencyToCurrency(money: Money, currency: Currency) =
        Money(money.count * currency.rate, currency)

    /**
     * @param money - money to convert
     * @param toCurrency - Currency to convert [money]
     */
    fun convertCurrency(money: Money, toCurrency: Currency): Money {
        val defCur = toDefaultCurrency(money)
        return fromDefaultCurrencyToCurrency(defCur, toCurrency)
    }

    /**
     * @param balance - balance to convert
     * @param currencies - currencies for convert
     *
     * @return list with balance which converted to all [currencies]
     */
    fun currentBalanceToAnotherCurrencies(balance: Money, currencies: List<Currency> = listOf(
        Currency.USD,
        Currency.EUR,
        Currency.RUR,
        Currency.GBP)): List<Money> {
        val rez = mutableListOf<Money>()

        for (currency in currencies) {
            if (balance.currency == currency)
                continue
            rez.add(convertCurrency(balance, currency))
        }
        return rez
    }

    /**
     * Convert money to [defaultCurrency]
     */
    fun toDefaultCurrency(money: Money): Money =
        Money(money.count / money.currency.rate, defaultCurrency)
}


/**
 *
 */
fun initCurrencies(context: Context, callback: () -> Unit) {
    val pref =
        context.getSharedPreferences(CurrenciesRateWorker.CurrenciesStorage, Context.MODE_PRIVATE)

    synchronized(pref) {


        val spChanged =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->

                lastCurrencyUpdateTime = sharedPreferences.getString("lastUpdateDate",
                    context.getString(R.string.not_update_yet))
                for (currency in Currency.values()) {
                    if (currency.toString() == key) {
                        currency.rate =
                                sharedPreferences.getFloat(currency.toString(), 0.0f).toDouble()
                        callback()
                        break
                    }
                }
            }

        pref.registerOnSharedPreferenceChangeListener(spChanged)

        lastCurrencyUpdateTime =
                pref.getString("lastUpdateDate", context.getString(R.string.not_update_yet))

        for (currency in Currency.values()) {
            currency.rate = pref.getFloat(currency.toString(), 0.0f).toDouble()
        }

    }

}