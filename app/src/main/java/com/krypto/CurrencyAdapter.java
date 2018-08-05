package com.krypto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.MyViewHolder> {

    private List<Currency> currencyList;
    private int card_count = 0;
    public int ITEM_POSITION = 0;

    public String BTC_CRYPTO_URL = "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=";
    public String ETH_CRYPTO_URL = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=";

    SharedPreferences sharedPref = null;
    SharedPreferences.Editor editor = null;

    private Context mContext;
    Handler loadPrimaryHandler = new Handler();
    final Handler loadOnDelayHandler = new Handler();
    final int delay = 25000;
    Currency currency;
    Resources res;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public CurrencyAdapter(Context mContext, List<Currency> currencyList) {
        this.mContext = mContext;
        this.currencyList = currencyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.currency_card, parent, false);
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.shared_pref_crypto), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        res = mContext.getResources();

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        currency = currencyList.get(position);
        holder.title.setText(currency.getName() + " 0.00");
        card_count = 0;

        // Inicjowana na starcie menu_main, pobiera kursy wybranych walut
        Runnable runnable = new Runnable() {
            public void run() {
                    loadPrimaryHandler.post(new Runnable(){
                        public void run() {
                            currency = currencyList.get(position);
                            String SELECT_BASE = "";

                            if (currency.getThumbnail()==R.drawable.btc_logo) {
                                SELECT_BASE = BTC_CRYPTO_URL;
                            } else {
                                SELECT_BASE = ETH_CRYPTO_URL;
                            }

                            new HttpRequestTask(
                                    new HttpRequest(SELECT_BASE+currency.getName(), HttpRequest.POST, "{ \"currency\": \"value\" }"),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String name = response.body.replaceAll("\"", "")
                                                        .replace("{", "").replace("}", "").split(":")[0];
                                                String value = response.body.replaceAll("\"", "")
                                                        .replace("{", "").replace("}", "").split(":")[1];

                                               holder.title.setText(name + " " + value);
                                               card_count++;

                                               Toast.makeText(mContext,"Wczytano aktualne kursy kryptowalut.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Log.e(this.getClass().toString(), "Zapytanie HTTP nieudane: " + response);
                                                Toast.makeText(mContext, "Błąd! Sprawdź połączenie z internetem.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).execute();
                        }
                    });
            }

        };

        new Thread(runnable).start();

        // Wczytuje nowe wartości dla walut co delay
        loadOnDelayHandler.postDelayed(new Runnable() {
            public void run() {
                currency = currencyList.get(position);
                String SELECT_BASE = "";

                if (currency.getThumbnail()==R.drawable.btc_logo) {
                    SELECT_BASE = BTC_CRYPTO_URL;
                } else {
                    SELECT_BASE = ETH_CRYPTO_URL;
                }

                new HttpRequestTask(
                        new HttpRequest(SELECT_BASE+currency.getName(), HttpRequest.POST, "{ \"currency\": \"value\" }"),
                        new HttpRequest.Handler() {

                            @Override
                            public void response(HttpResponse response) {
                                if (response.code == 200) {
                                    String name = response.body.replaceAll("\"", "")
                                            .replace("{", "").replace("}", "").split(":")[0];
                                    String value = response.body.replaceAll("\"", "")
                                            .replace("{", "").replace("}", "").split(":")[1];

                                    holder.title.setText(name+" "+value);
                                    card_count++;

                                    Toast.makeText(mContext,"Wczytano aktualne kursy kryptowalut.", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(this.getClass().toString(), "Zapytanie HTTP nieudane: " + response);
                                    Toast.makeText(mContext, "Błąd! Sprawdź połączenie z internetem.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).execute();

                loadOnDelayHandler.postDelayed(this, delay);
            }
        }, delay);

        // Załadowanie ikon dla krypto
        Glide.with(mContext).load(currency.getThumbnail()).into(holder.thumbnail);

        // Otwarcie conversion_form po kliknięciu w kartę
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent theIntent = new Intent(mContext, ConversionActivity.class);
                theIntent.putExtra("crypto_position", position);
                mContext.startActivity(theIntent);
                MainActivity mainActivity = new MainActivity();
                mainActivity.finish();

            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent theIntent = new Intent(mContext, ConversionActivity.class);
                theIntent.putExtra("crypto_position", position);
                mContext.startActivity(theIntent);
                MainActivity mainActivity = new MainActivity();
                mainActivity.finish();

            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
                ITEM_POSITION = position;
            }
        });
    }

    // Otwarcie podmenu dla karty
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_currency, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    // Funkcjonalności dla podmenu
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_conversion:

                    Intent theIntent = new Intent(mContext, ConversionActivity.class);
                    theIntent.putExtra("crypto_position", ITEM_POSITION);
                    mContext.startActivity(theIntent);
                    MainActivity mainActivity = new MainActivity();
                    mainActivity.finish();
                    return true;

                case R.id.action_remove:
                    int count  = currencyList.size() - 1;
                    currencyList.remove(ITEM_POSITION);

                    for (int i = 0; i< currencyList.size(); i++) {
                            editor.remove("List_" + i);
                            editor.putString("List_" + i, currencyList.get(i).getName() + "#" +currencyList.get(i).getSymbol() + "#" + currencyList.get(i).getThumbnail());
                    }

                    editor.putInt("List_size", count);
                    editor.commit();

                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();

                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }
}