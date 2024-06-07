package com.cookandroid.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.login.Fragment.ImagePredict;
import com.cookandroid.login.Fragment.UserMemos;
import com.cookandroid.login.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<Object> menuList; // ImagePredict와 UserMemos를 모두 처리할 수 있는 List

    public MenuAdapter(List<Object> menuList) {
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Object item = menuList.get(position);
        if (item instanceof ImagePredict) { // ImagePredict일 경우 처리
            ImagePredict imagePredict = (ImagePredict) item;
            String timeOnly = imagePredict.getDateTime().split(" ")[1]; // 시간만 가져오기
            holder.dateTextView.setText(timeOnly);
            holder.mealTimeTextView.setText(imagePredict.getEatTime());
            holder.menuNameTextView.setText(imagePredict.getImageName());
        } else if (item instanceof UserMemos) { // UserMemos일 경우 처리
            UserMemos userMemos = (UserMemos) item;
            holder.dateTextView.setText(userMemos.getRealTime());
            holder.mealTimeTextView.setText("Custom meal time1"); // 예시로 임의의 값 설정
            holder.menuNameTextView.setText("Custom meal time2");
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView mealTimeTextView;
        TextView menuNameTextView;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            mealTimeTextView = itemView.findViewById(R.id.mealTimeTextView);
            menuNameTextView = itemView.findViewById(R.id.menuNameTextView);
        }
    }
}
