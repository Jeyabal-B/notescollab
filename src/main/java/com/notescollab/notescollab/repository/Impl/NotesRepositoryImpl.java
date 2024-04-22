package com.notescollab.notescollab.repository.Impl;

import com.notescollab.notescollab.entity.NotesDetails;
import com.notescollab.notescollab.repository.NotesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.sql.DriverManager.getConnection;

@Repository
public class NotesRepositoryImpl implements NotesRepository {

    private static final Logger logger = LoggerFactory.getLogger("com.notescollab.notescollab.repository.Impl.NotesRepositoryImpl");
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String ADD_NOTES_QUERY = "INSERT INTO public.notes (title,description, notescontent,userid,createdon) VALUES(?,?,?,?,?)";
    private static final String GET_NOTES_LIST_QUERY = "select * from notes n where userid = ?";
    private static final String GET_NOTES_BY_ID = "select * from notes n where userid = ? and n.id = ?";
    private static final String UPDATE_NOTES_BY_ID = "update notes set title = ?,description = ?, notescontent = ?, createdon = ? where id = ? and userid = ?";

    private static final String DELETE_NOTES_BY_ID = "DELETE FROM notes where id =? and userid = ?";

    @Override
    public String createNotes(NotesDetails notes, Long userId) {

        KeyHolder keyHolder = new GeneratedKeyHolder ();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(ADD_NOTES_QUERY, new String[]{"id"});
            ps.setString(1, notes.getTitle ());
            ps.setString ( 2, notes.getDescription ());
            ps.setString ( 3, notes.getNotescontent ());
            ps.setLong ( 4, userId);
            ps.setTimestamp ( 5, new Timestamp ( System.currentTimeMillis () ) );

            return ps;
        }, keyHolder);


        int generatedId = keyHolder.getKey().intValue ();

        return "Notes successfully created with id- " + generatedId;
    }

    @Override
    public List<NotesDetails> getNotesList(Long userId) throws Exception {
        logger.info ("getNotesList:: get all the notes list for the authenticated user- " +userId );

        Connection conn = jdbcTemplate.getDataSource ().getConnection ();
        try {

            List<NotesDetails> notesList = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement ( GET_NOTES_LIST_QUERY );
            ps.setLong ( 1,userId );
            ResultSet rs = ps.executeQuery ();

            while(rs.next ()){
                NotesDetails notesDetails = new NotesDetails ();
                notesDetails.setId ( rs.getLong ( "id" ) );
                notesDetails.setTitle ( rs.getString ( "title" ) );
                notesDetails.setDescription ( rs.getString ( "description" ) );
                notesDetails.setNotescontent ( rs.getString ( "notescontent" ) );
                notesDetails.setCreatedon ( rs.getTimestamp ( "createdon") );

                notesList.add ( notesDetails );
            }
            return notesList;

        }catch (Exception e){
            logger.error ( "getNotesList:: Got Exception while getting Notes list"+e.getMessage (),e );
            throw new Exception (e.getMessage ());
        }finally {
            logger.info ( "getNotesList:: closing connection ..." );
            conn.close();
        }
    }

    @Override
    public NotesDetails getNotesById(Integer id, Long userId) throws Exception {
        logger.info ( "getNotesDetails:: getting notes details");
        try {
            return jdbcTemplate.queryForObject ( GET_NOTES_BY_ID, (rs , rowNum) -> {
                return new NotesDetails (
                        rs.getLong ( "id"),
                        rs.getString ( "title" ),
                        rs.getString ( "description" ),
                        rs.getString ( "notescontent"),
                        rs.getTimestamp ( "createdon" )
                );

            },userId,id);

        }catch (Exception e) {
            logger.error ( "getNotesDetails:: Got exception while getting notes details by id  "+e.getMessage (),e );
            throw new Exception (e.getMessage ());
        }

    }

    @Override
    public String updateNotesById(NotesDetails notes, Integer id, Long userId) throws Exception {
        logger.info("updateNotesById:: updating notes details...");
        try{

            String result = "";
            Integer rs = jdbcTemplate.update (conn -> {
                PreparedStatement ps = conn.prepareStatement (UPDATE_NOTES_BY_ID );
                ps.setString (1, notes.getTitle ());
                ps.setString (2, notes.getDescription ());
                ps.setString (3, notes.getNotescontent ());
                ps.setTimestamp ( 4, new Timestamp ( System.currentTimeMillis() ) );
                ps.setLong (5, id);
                ps.setLong ( 6, userId );
                return ps;
            });

            if(rs.intValue () != 0){
                result = "Notes updated successfully!";
            }
            return result;

        }catch (Exception e){
            logger.error("updateNotesById:: Got Exceptin while updating notes by id "+e.getMessage(), e);
            throw new Exception (e.getMessage ());
        }
    }

    @Override
    public String deleteNotesById(Integer id, Long userId) throws Exception {
        logger.info("deleteNotesById:: deleting notes by id "+id);
        try {
            String result = "";
            Integer rs = jdbcTemplate.update (conn -> {
                PreparedStatement ps = conn.prepareStatement (DELETE_NOTES_BY_ID);
                ps.setInt ( 1, id );
                ps.setLong ( 2, userId );
                return ps;
            });
            if(rs.intValue () != 0){
                result = "Notes deleted successfully";
            }
            return result;
        }catch (Exception e){
            logger.error("deleteNotesById:: Got exceptiono while deleting notes"+e.getMessage(),e);
            throw new Exception(e.getMessage ());
        }
    }
}
