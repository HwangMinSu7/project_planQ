package com.example.ex76.dto;

import com.example.ex76.entity.BoardComment;
import com.example.ex76.entity.BoardPost;
import com.example.ex76.entity.MeetupParticipant;

import java.util.List;
import java.util.Set;

public record BoardDetailDTO(
    BoardPost post,
    List<BoardComment> comments,
    List<MeetupParticipant> participants,
    boolean joined,
    boolean owner,
    boolean liked,
    boolean admin,
    Set<Long> likedCommentIds
) {}
