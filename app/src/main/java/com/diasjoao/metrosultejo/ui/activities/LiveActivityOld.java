package com.diasjoao.metrosultejo.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.diasjoao.metrosultejo.R;
import com.diasjoao.metrosultejo.data.JsonHandler;
import com.diasjoao.metrosultejo.utils.DateUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class LiveActivityOld extends AppCompatActivity {

    Spinner spinner_1, spinner_2;
    ConstraintLayout board1, board2, board3;
    TextView hours1, hours2, hours3;
    TextView minutes1, minutes2, minutes3;
    AdView mAdView;

    private int line;
    private int station;

    private int stationId, stationNumber, stationOffset;
    private int lineId;
    private Boolean inverse = true;
    private ArrayList<Date> stationTimes = new ArrayList<>();
    private ArrayList<Long> timeDiferences = new ArrayList<>(3);
    private ArrayList<String> realTimes = new ArrayList<>(3);
    private int[] timerOnOff = new int[]{-1,-1,-1};

    private Calendar rightNow;
    private Boolean isSummer = true;
    private String dayOfTheWeek;

    CountDownTimer firstTimer = null;
    CountDownTimer secondTimer = null;
    CountDownTimer thirdTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_old);
        
        initializeAds();
        initializeViews();

        // set initial line and station
        Intent intent = getIntent();
        line = intent.getIntExtra("line", 0);
        station = intent.getIntExtra("station", 0);

        rightNow = Calendar.getInstance();
        rightNow.add(Calendar.HOUR, -3);
        // set season and day of the week
        setTimeSettings(rightNow);

        // set available lines on spinner 1
        ArrayAdapter<CharSequence> adapter_1 = ArrayAdapter.createFromResource(this,
                R.array.linhas, android.R.layout.simple_spinner_item);
        adapter_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_1.setAdapter(adapter_1);

        // clicker handler on spinner 1
        spinner_1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayAdapter<CharSequence> adapter_2 = null;

                if (i == 0) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_11, android.R.layout.simple_spinner_item);
                }
                if (i == 1) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_12, android.R.layout.simple_spinner_item);
                }
                if (i == 2) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_21, android.R.layout.simple_spinner_item);
                }
                if (i == 3) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_22, android.R.layout.simple_spinner_item);
                }
                if (i == 4) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_31, android.R.layout.simple_spinner_item);
                }
                if (i == 5) {
                    adapter_2 = ArrayAdapter.createFromResource(getBaseContext(),
                            R.array.linha_32, android.R.layout.simple_spinner_item);
                }

                // set available stations on spinner 2
                adapter_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_2.setAdapter(adapter_2);
                station = station >= adapter_2.getCount() ? adapter_2.getCount() - 1 : station;
                // set default station on spinner 2
                spinner_2.setSelection(station);

                // update line selected
                line = i;
                lineId = getLineId(line);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // clicker handler on spinner 2
        spinner_2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                rightNow = Calendar.getInstance();
                rightNow.add(Calendar.HOUR, -3);

                try {
                    JSONObject obj = new JSONObject(JsonHandler.loadJSONFromAsset(getResources().openRawResource(R.raw.dataold)));

                    // get station Id in Json file
                    stationId = JsonHandler.getStationId(obj, (String) adapterView.getItemAtPosition(i));
                    station = i;

                    // get station position in the metro line
                    stationNumber = JsonHandler.getStationNumber(obj, lineId, stationId, inverse);
                    // get station offset to the beginning of the metro line
                    stationOffset = JsonHandler.getStationOffsetOld(obj, lineId, stationNumber, inverse);

                    // get all times for specific station
                    stationTimes = getStationTimes(obj, stationOffset);
                    // calculate three closest time differences from actual time
                    timeDiferences = setTimeDifferences(rightNow, stationTimes);

                    // cancel previous timers if running
                    if (timerOnOff[0] != -1) {
                        firstTimer.cancel();
                        timerOnOff[0] = -1;
                    }
                    if (timerOnOff[1] != -1) {
                        secondTimer.cancel();
                        timerOnOff[1] = -1;
                    }
                    if (timerOnOff[2] != -1) {
                        thirdTimer.cancel();
                        timerOnOff[2] = -1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Timer #1
                if (timeDiferences.get(0) != -1) {
                    setFirstTimer();
                } else {
                    board1.setVisibility(View.GONE);
                }

                // Timer #2
                if (timeDiferences.get(1) != -1) {
                    setSecondTimer();
                } else {
                    hours2.setText(getResources().getString(R.string.volte_amanha));
                    hours2.setBackgroundColor(Color.TRANSPARENT);
                    minutes2.setText("    ");
                    minutes2.setBackgroundColor(Color.TRANSPARENT);
                }

                // Timer #3
                if (timeDiferences.get(2) != -1) {
                    setThirdTimer();
                } else {
                    board3.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // select initial line (starts workflow)
        spinner_1.setSelection(line);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (timerOnOff[0] != -1) {
            firstTimer.cancel();
            timerOnOff[0] = -1;
        }
        if (timerOnOff[1] != -1) {
            secondTimer.cancel();
            timerOnOff[1] = -1;
        }
        if (timerOnOff[2] != -1) {
            thirdTimer.cancel();
            timerOnOff[2] = -1;
        }
    }

    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void initializeViews() {
        spinner_1 = (Spinner) findViewById(R.id.linhas);
        spinner_2 = (Spinner) findViewById(R.id.estacoes);

        board1 = findViewById(R.id.board1);
        board2 = findViewById(R.id.board2);
        board3 = findViewById(R.id.board3);

        hours1 = findViewById(R.id.hours1);
        hours2 = findViewById(R.id.hours2);
        hours3 = findViewById(R.id.hours3);

        minutes1 = findViewById(R.id.minutes1);
        minutes2 = findViewById(R.id.minutes2);
        minutes3 = findViewById(R.id.minutes3);
    }

    private void setFirstTimer() {
        board1.setVisibility(View.VISIBLE);
        hours1.setText(realTimes.get(0));
        timerOnOff[0] = 1;
        try{
            firstTimer = new CountDownTimer(1000000, 1000) {

                Long temp = -timeDiferences.get(0);

                public void onTick(long millisUntilFinished) {
                    temp = temp + 1000;
                    minutes1.setText('+' + DateUtils.millisecondsToString(temp));
                }

                public void onFinish() {

                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setSecondTimer() {
        hours2.setText(realTimes.get(1));
        timerOnOff[1] = 1;
        try{
            secondTimer = new CountDownTimer(timeDiferences.get(1), 1000) {

                public void onTick(long millisUntilFinished) {
                    minutes2.setText(DateUtils.millisecondsToString(millisUntilFinished));
                }

                public void onFinish() {
                    Intent intent = new Intent(getBaseContext(), LiveActivityOld.class);
                    intent.putExtra("line", line);
                    intent.putExtra("station", station);
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish();
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setThirdTimer() {
        board3.setVisibility(View.VISIBLE);
        hours3.setText(realTimes.get(2));
        timerOnOff[2] = 1;
        try{
            thirdTimer = new CountDownTimer(timeDiferences.get(2), 1000) {

                public void onTick(long millisUntilFinished) {
                    minutes3.setText(DateUtils.millisecondsToString(millisUntilFinished));
                }

                public void onFinish() {

                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getLineId(int line) {
        switch (line) {
            case 0:
                inverse = false;
                return 0;
            case 1:
                inverse = true;
                return 0;
            case 2:
                inverse = false;
                return 1;
            case 3:
                inverse = true;
                return 1;
            case 4:
                inverse = false;
                return 2;
            case 5:
                inverse = true;
                return 2;
            default:
                return -1;
        }
    }

    public ArrayList<Date> getStationTimes(JSONObject jsonFile, int stationOffset) throws JSONException {
        ArrayList<Date> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        JSONObject line = jsonFile.getJSONArray("lines").getJSONObject(lineId);
        JSONObject directions = line.getJSONArray("directions").getJSONObject(inverse ? 1 : 0);

        int combination;
        if (isSummer) {
            if (dayOfTheWeek.equals("weekdays")) {
                combination = 0;
            } else if (dayOfTheWeek.equals("saturdays")) {
                combination = 1;
            } else {
                combination = 2;
            }
        } else {
            if (dayOfTheWeek.equals("weekdays")) {
                combination = 3;
            } else if (dayOfTheWeek.equals("saturdays")) {
                combination = 4;
            } else {
                combination = 5;
            }
        }

        JSONObject departures = directions.getJSONArray("departures").getJSONObject(combination);
        JSONArray times = departures.getJSONObject("times").getJSONArray("times");

        for (int i = 0; i < times.length(); i++) {
            try {
                result.add(new Date(dateFormat.parse(times.getString(i)).getTime() + (stationOffset * DateUtils.ONE_MINUTE_IN_MILLIS)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void setTimeSettings(Calendar rightNow) {
        if (rightNow.get(Calendar.DAY_OF_MONTH) >= 8 && rightNow.get(Calendar.MONTH) == Calendar.SEPTEMBER) {
            isSummer = false;
        }
        if (rightNow.get(Calendar.DAY_OF_MONTH) <= 14 && rightNow.get(Calendar.MONTH) == Calendar.JULY){
            isSummer = false;
        }
        if (rightNow.get(Calendar.MONTH) > Calendar.SEPTEMBER || rightNow.get(Calendar.MONTH) < Calendar.JULY) {
            isSummer = false;
        }

        switch (rightNow.get(Calendar.DAY_OF_WEEK)) {
            case 7:
                dayOfTheWeek = "saturdays";
                break;
            case 1:
                dayOfTheWeek = "sundays";
                break;
            default:
                dayOfTheWeek = "weekdays";
                break;
        }

        dayOfTheWeek = DateUtils.checkHoliday(rightNow, Arrays.asList(getResources().getStringArray(R.array.feriados))) ? "sundays" : dayOfTheWeek;
    }

    public ArrayList<Long> setTimeDifferences(Calendar rightNowCalendar, ArrayList<Date> stationTimes) throws ParseException {
        ArrayList<Long> result = new ArrayList<>(3);
        String temp = rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + rightNowCalendar.get(Calendar.MINUTE);
        Date rightNow = new SimpleDateFormat("HH:mm").parse(temp);

        realTimes.clear();

        // Apenas metros no futuro
        if (stationTimes.get(0).getTime() - rightNow.getTime() > 0) {
            result.add((long) -1);
            realTimes.add("-1");
            result.add(stationTimes.get(0).getTime() - rightNow.getTime());
            rightNowCalendar.setTime(stationTimes.get(0));
            rightNowCalendar.add(Calendar.HOUR, 3);
            realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));
            result.add(stationTimes.get(1).getTime() - rightNow.getTime());
            rightNowCalendar.setTime(stationTimes.get(1));
            rightNowCalendar.add(Calendar.HOUR, 3);
            realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));
        } else {
            int cont = 0;
            int position = -1;
            for (Date d : stationTimes) {
                if (d.getTime() - rightNow.getTime() > 0 && position == -1) {
                    position = cont;
                }
                cont++;
            }

            if (position != -1) {
                if (position == stationTimes.size()-1) {
                    // falta apenas um metro no dia
                    result.add(stationTimes.get(position-1).getTime() - rightNow.getTime());
                    rightNowCalendar.setTime(stationTimes.get(position-1));
                    rightNowCalendar.add(Calendar.HOUR, 3);
                    realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                    result.add(stationTimes.get(position).getTime() - rightNow.getTime());
                    rightNowCalendar.setTime(stationTimes.get(position));
                    rightNowCalendar.add(Calendar.HOUR, 3);
                    realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                    result.add((long) -1);
                    realTimes.add("-1");
                } else {
                    // horarios normais
                    result.add(stationTimes.get(position-1).getTime() - rightNow.getTime());
                    rightNowCalendar.setTime(stationTimes.get(position-1));
                    rightNowCalendar.add(Calendar.HOUR, 3);
                    realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                    result.add(stationTimes.get(position).getTime() - rightNow.getTime());
                    rightNowCalendar.setTime(stationTimes.get(position));
                    rightNowCalendar.add(Calendar.HOUR, 3);
                    realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                    result.add(stationTimes.get(position+1).getTime() - rightNow.getTime());
                    rightNowCalendar.setTime(stationTimes.get(position+1));
                    rightNowCalendar.add(Calendar.HOUR, 3);
                    realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                }
            } else {
                // Não vão passar mais metros
                result.add(stationTimes.get(stationTimes.size()-1).getTime() - rightNow.getTime());
                rightNowCalendar.setTime(stationTimes.get(stationTimes.size()-1));
                rightNowCalendar.add(Calendar.HOUR, 3);
                realTimes.add(rightNowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", rightNowCalendar.get(Calendar.MINUTE)));

                result.add((long) -1);
                realTimes.add("-1");

                result.add((long) -1);
                realTimes.add("-1");
            }
        }

        return result;
    }
}
