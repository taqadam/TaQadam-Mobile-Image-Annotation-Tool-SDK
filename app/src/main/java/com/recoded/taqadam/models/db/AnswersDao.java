package com.recoded.taqadam.models.db;

import com.recoded.taqadam.models.Answer;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AnswersDao {
    @Query("SELECT * FROM answers where assignment_id LIKE  :assignmentId AND task_id LIKE :taskId")
    LiveData<List<Answer>> getAnswer(Long assignmentId, Long taskId);

    @Insert
    void saveAnswer(Answer a);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAnswer(Answer a);

    @Delete
    void deleteAnswer(Answer a);
}
