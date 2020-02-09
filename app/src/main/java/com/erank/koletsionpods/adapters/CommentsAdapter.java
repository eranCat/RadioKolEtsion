package com.erank.koletsionpods.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.koletsionpods.R;
import com.erank.koletsionpods.db.models.Comment;
import com.erank.koletsionpods.utils.listeners.OnCommentClickCallback;

import java.util.Date;
import java.util.List;

public class CommentsAdapter extends ListAdapter<Comment, CommentsAdapter.CommentHolder> {

    private OnCommentClickCallback callback;

    public CommentsAdapter(List<Comment> comments) {
        super(new CommentsDiffUtilCallback());
        submitList(comments);
    }

    public void setCallback(OnCommentClickCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        holder.fill(getItem(position));
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        private final View userBtns, removeBtn, editBtn;
        private final TextView userTv, contentTv, dateTv;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            userTv = itemView.findViewById(R.id.comment_op);
            contentTv = itemView.findViewById(R.id.comment_content);
            dateTv = itemView.findViewById(R.id.comment_date_tv);
            userBtns = itemView.findViewById(R.id.comment_user_btns);
            editBtn = itemView.findViewById(R.id.comment_edit_btn);
            removeBtn = itemView.findViewById(R.id.comment_removeBtn);
        }

        private CharSequence getFormattedDate(Date date) {
//            DateFormat dateFormat = SimpleDateFormat
//                    .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//            return dateFormat.format(date);
            return DateUtils.getRelativeTimeSpanString(date.getTime());
        }

        void fill(Comment comment) {
            itemView.setOnClickListener(v -> callback
                    .onItemClicked(comment, getAdapterPosition()));

            userTv.setText(comment.getUserName());
            contentTv.setText(comment.getContent());
            dateTv.setText(getFormattedDate(comment.getPostDate()));

            if (!comment.isEditable()) {
                userBtns.setVisibility(View.GONE);
                return;
            }

            userBtns.setVisibility(View.VISIBLE);

            editBtn.setOnClickListener(v -> callback
                    .onItemEdit(comment, getAdapterPosition()));

            removeBtn.setOnClickListener(v -> callback
                    .onRemoveClicked(comment, getAdapterPosition()));
        }
    }
}