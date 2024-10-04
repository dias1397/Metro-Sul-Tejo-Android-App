package com.diasjoao.metrosultejo.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diasjoao.metrosultejo.R;
import com.diasjoao.metrosultejo.adapters.TimetableAdapter;
import com.diasjoao.metrosultejo.data.repository.ScheduleRepository;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerView;

    private int seasonId = 1;
    private int dayId = 1;
    private int lineId = 1;
    private TimetableAdapter timetableAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        initVars();
        initViews(view);

        setupUI();

        return view;
    }

    private void initVars() {
        if (getArguments() != null) {
            seasonId = getArguments().getInt("seasonId", 1);
            dayId = getArguments().getInt("dayId", 1);
            lineId = getArguments().getInt("lineId", 1);
        }

        ScheduleRepository scheduleRepository = new ScheduleRepository(requireContext());
        timetableAdapter = new TimetableAdapter(
                scheduleRepository.findStationsBySeasonAndDayAndLine(seasonId, dayId, lineId)
        );
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    private void setupUI() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(timetableAdapter);
    }
}