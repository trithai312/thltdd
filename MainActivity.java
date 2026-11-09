package com.example.mynotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    EditText edtSearch;
    LinearLayout layoutNotes;
    TextView btnAdd;

    ArrayList<Note> notes = new ArrayList<>();
    SharedPreferences preferences;

    // Lớp lưu thông tin ghi chú
    static class Note {
        String title;
        String content;
        String date;

        Note(String title, String content, String date) {
            this.title = title;
            this.content = content;
            this.date = date;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        edtSearch = findViewById(R.id.edtSearch);
        layoutNotes = findViewById(R.id.layoutNotes);
        btnAdd = findViewById(R.id.btnAdd);

        preferences = getSharedPreferences(
                "MyNotesData",
                MODE_PRIVATE
        );

        loadNotes();
        displayNotes("");

        // Thêm ghi chú
        btnAdd.setOnClickListener(v -> {
            showNoteDialog(-1);
        });

        // Tìm kiếm ghi chú
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start,
                    int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start,
                    int before, int count) {
                displayNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Chuyển dp sang pixel
    int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }

    // Tạo nền bo góc
    GradientDrawable createBackground(
            int color, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    // Lấy ngày giờ hiện tại
    String getCurrentDate() {
        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                );

        return format.format(new Date());
    }

    // Hiển thị danh sách ghi chú
    void displayNotes(String keyword) {

        layoutNotes.removeAllViews();

        int count = 0;

        int[] colors = {
                0xFFF0E5FF,
                0xFFFFEBCD,
                0xFFDDF5E8,
                0xFFFFE2EB,
                0xFFE0F0FF
        };

        for (int i = notes.size() - 1; i >= 0; i--) {

            Note note = notes.get(i);

            String search = keyword.toLowerCase(Locale.ROOT);

            if (!note.title.toLowerCase(Locale.ROOT).contains(search)
                    && !note.content.toLowerCase(Locale.ROOT)
                    .contains(search)) {
                continue;
            }

            count++;

            final int index = i;

            // Card ghi chú
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(
                    dp(18), dp(18), dp(18), dp(18)
            );

            card.setBackground(
                    createBackground(
                            colors[i % colors.length], 20
                    )
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            -1, -2
                    );

            cardParams.bottomMargin = dp(15);
            layoutNotes.addView(card, cardParams);

            // Tiêu đề
            TextView txtTitle = new TextView(this);
            txtTitle.setText(note.title);
            txtTitle.setTextSize(19);
            txtTitle.setTextColor(Color.rgb(65, 52, 85));
            txtTitle.setTypeface(null, Typeface.BOLD);

            card.addView(txtTitle);

            // Nội dung
            TextView txtContent = new TextView(this);
            txtContent.setText(note.content);
            txtContent.setTextSize(15);
            txtContent.setTextColor(Color.rgb(95, 85, 110));
            txtContent.setMaxLines(4);

            LinearLayout.LayoutParams contentParams =
                    new LinearLayout.LayoutParams(-1, -2);

            contentParams.topMargin = dp(10);
            card.addView(txtContent, contentParams);

            // Ngày tạo / cập nhật
            TextView txtDate = new TextView(this);
            txtDate.setText("🕒 " + note.date);
            txtDate.setTextSize(12);
            txtDate.setTextColor(Color.rgb(125, 115, 140));

            LinearLayout.LayoutParams dateParams =
                    new LinearLayout.LayoutParams(-1, -2);

            dateParams.topMargin = dp(15);
            card.addView(txtDate, dateParams);

            // Nhấn để chỉnh sửa
            card.setOnClickListener(v -> {
                showNoteDialog(index);
            });

            // Nhấn giữ để xóa
            card.setOnLongClickListener(v -> {
                deleteNote(index);
                return true;
            });
        }

        // Không có ghi chú
        if (count == 0) {

            TextView txtEmpty = new TextView(this);

            txtEmpty.setText(
                    "🌸 Chưa có ghi chú nào!\n" +
                            "Nhấn dấu + để bắt đầu nhé!"
            );

            txtEmpty.setTextSize(17);
            txtEmpty.setTextColor(Color.rgb(145, 125, 165));
            txtEmpty.setGravity(Gravity.CENTER);
            txtEmpty.setPadding(0, dp(80), 0, 0);

            layoutNotes.addView(txtEmpty);
        }
    }

    // Hộp thoại thêm hoặc sửa ghi chú
    void showNoteDialog(int index) {

        boolean isEdit = index >= 0;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(
                dp(20), dp(10), dp(20), 0
        );

        // Nhập tiêu đề
        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Tiêu đề ghi chú");
        inputTitle.setTextSize(17);
        inputTitle.setSingleLine(true);

        layout.addView(inputTitle);

        // Nhập nội dung
        EditText inputContent = new EditText(this);
        inputContent.setHint("Viết điều gì đó thú vị...");
        inputContent.setTextSize(15);
        inputContent.setGravity(Gravity.TOP);
        inputContent.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        -1, dp(180)
                );

        contentParams.topMargin = dp(15);
        layout.addView(inputContent, contentParams);

        // Nếu sửa thì hiện nội dung cũ
        if (isEdit) {
            inputTitle.setText(notes.get(index).title);
            inputContent.setText(notes.get(index).content);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(
                        isEdit ? "✏️ Chỉnh sửa ghi chú"
                                : "🌸 Ghi chú mới"
                )
                .setView(layout)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(view -> {

                String title = inputTitle.getText()
                        .toString().trim();

                String content = inputContent.getText()
                        .toString().trim();

                if (title.isEmpty()) {
                    inputTitle.setError(
                            "Vui lòng nhập tiêu đề!"
                    );
                    return;
                }

                if (isEdit) {
                    Note note = notes.get(index);
                    note.title = title;
                    note.content = content;
                    note.date = getCurrentDate();

                } else {
                    notes.add(new Note(
                            title,
                            content,
                            getCurrentDate()
                    ));
                }

                saveNotes();

                displayNotes(
                        edtSearch.getText().toString()
                );

                Toast.makeText(
                        this,
                        "Đã lưu ghi chú thành công!",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // Xóa ghi chú
    void deleteNote(int index) {

        new AlertDialog.Builder(this)
                .setTitle("🗑 Xóa ghi chú")
                .setMessage(
                        "Bạn có chắc chắn muốn xóa ghi chú này?"
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {

                    notes.remove(index);
                    saveNotes();

                    displayNotes(
                            edtSearch.getText().toString()
                    );

                    Toast.makeText(
                            this,
                            "Đã xóa ghi chú!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    // Lưu ghi chú vào bộ nhớ máy
    void saveNotes() {

        JSONArray array = new JSONArray();

        try {

            for (Note note : notes) {

                JSONObject object = new JSONObject();

                object.put("title", note.title);
                object.put("content", note.content);
                object.put("date", note.date);

                array.put(object);
            }

            preferences.edit()
                    .putString("notes", array.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đọc ghi chú đã lưu
    void loadNotes() {

        notes.clear();

        String data = preferences.getString(
                "notes", "[]"
        );

        try {

            JSONArray array = new JSONArray(data);

            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);

                notes.add(new Note(
                        object.optString("title"),
                        object.optString("content"),
                        object.optString("date")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
