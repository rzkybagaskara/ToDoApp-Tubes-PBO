package com.kelompok5.todoapp.activity;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.kelompok5.todoapp.R;
import com.kelompok5.todoapp.adapter.TaskAdapter;
import com.kelompok5.todoapp.bottomSheetFragment.CreateTaskBottomSheetFragment;
import com.kelompok5.todoapp.bottomSheetFragment.ShowCalendarViewBottomSheet;
import com.kelompok5.todoapp.broadcastReceiver.AlarmBroadcastReceiver;
import com.kelompok5.todoapp.database.DatabaseClient;
import com.kelompok5.todoapp.model.Task;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements CreateTaskBottomSheetFragment.setRefreshListener {
    //Deklarasi object dari layout main
    @BindView(R.id.taskRecycler)
    RecyclerView taskRecycler;
    @BindView(R.id.addTask)
    TextView addTask;
    TaskAdapter taskAdapter;
    @BindView(R.id.search)
    SearchView searchView;
    //List of tasks
    List<Task> tasks = new ArrayList<>();
    String search;
    @BindView(R.id.noDataImage)
    ImageView noDataImage;
    @BindView(R.id.calendar)
    ImageView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpAdapter(); //Menyiapkan adapter untuk menampung task
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ComponentName receiver = new ComponentName(this, AlarmBroadcastReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Glide.with(getApplicationContext()).load(R.drawable.clipboard).into(noDataImage);

        //Memunculkan form pembuatan task baru jika 'add task' diklik
        addTask.setOnClickListener(view -> {
            //Bottomsheet menandakan bahwa task akan muncul dari bawah
            CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
            createTaskBottomSheetFragment.setTaskId(0, false, this, MainActivity.this);
            createTaskBottomSheetFragment.show(getSupportFragmentManager(), createTaskBottomSheetFragment.getTag());
        });

        getSavedTasks(null); //Mengambil task yang disimpan di dalam database

        //Memunculkan kalendar ketika ikon kalendar diklik
        calendar.setOnClickListener(view -> {
            //Kalendar akan muncul dari bawah
            ShowCalendarViewBottomSheet showCalendarViewBottomSheet = new ShowCalendarViewBottomSheet();
            showCalendarViewBottomSheet.show(getSupportFragmentManager(), showCalendarViewBottomSheet.getTag());
        });

        // SEARCH FUNCTION BELOW;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //Menginput tiap query yang diketik ke fungsi getsavedtasks
            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                getSavedTasks(newText);
                return false;
            }
        });
    }

    public void setUpAdapter() {
        taskAdapter = new TaskAdapter(this, tasks, this);
        taskRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        taskRecycler.setAdapter(taskAdapter);
    }

    private void getSavedTasks(String searchf) {
        searchf = searchf != null ? searchf : "";
        searchf = searchf + "%";

        String finalSearchf = searchf;

        class GetSavedTasks extends AsyncTask<Void, Void, List<Task>> {
            //Method untuk mengambil task dari database ke dalam task list
            @Override
            protected List<Task> doInBackground(Void... voids) {
                tasks = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .dataBaseAction()
                        .getTaskListSearch(finalSearchf); //Mengambil seluruhnya jika tidak ada query
                return tasks;
            }

            @Override
            protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);
                noDataImage.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
                setUpAdapter();
            }
        }

        GetSavedTasks savedTasks = new GetSavedTasks();
        savedTasks.execute();
    }

    @Override
    //Refresh task
    public void refresh() {
        getSavedTasks(search);
    }
}
