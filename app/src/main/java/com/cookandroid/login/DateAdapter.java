package com.cookandroid.login;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<String> dates;
    private OnDateClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnDateClickListener {
        void onDateClick(int position);
    }

    public DateAdapter(List<String> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;

        // 현재 날짜 구하기
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // 현재 날짜가 포함된 리스트의 인덱스 구하기
        int index = dates.indexOf(currentDate);

        // 선택된 위치로 설정
        if (index != -1) {
            selectedPosition = index;
        }
    }



    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dates.get(position);
        holder.bind(date, position);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public class DateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Button dateButton;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateButton = itemView.findViewById(R.id.dateButton);
            dateButton.setOnClickListener(this); // 버튼에 클릭 리스너 설정
        }

        public void bind(String date, int position) {
            dateButton.setText(date);
            // 선택된 위치와 현재 위치가 같으면 선택 상태로 표시
            if (position == selectedPosition) {
                dateButton.setBackgroundResource(R.drawable.light_date_button_background);
            } else {
                dateButton.setBackgroundResource(R.drawable.red_date_button_background);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                String selectedDate = dates.get(position);
                Toast.makeText(itemView.getContext(), "Selected date: " + selectedDate, Toast.LENGTH_SHORT).show();
                // 이전에 선택된 항목을 red_date_button_background로 변경
                notifyItemChanged(selectedPosition);
                // 선택된 위치 변경
                selectedPosition = position;
                // 선택된 항목을 light_date_button_background로 변경
                notifyItemChanged(selectedPosition);
                // 리스너 호출
                listener.onDateClick(position);
            }
        }
    }
}
