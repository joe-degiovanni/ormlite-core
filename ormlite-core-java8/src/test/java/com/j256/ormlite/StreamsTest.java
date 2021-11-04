package com.j256.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.LazyForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class StreamsTest extends BaseCoreTest {

    @Test
    public void testJpa() throws SQLException {
        //given
        connectionSource = spy(connectionSource);
        Dao<Lazy, Integer> lazyDao = createDao(Lazy.class, true);
        Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);
        Lazy entityWithFourChildren = of(new Foreign(), new Foreign(), new Foreign(), new Foreign());
        lazyDao.create(entityWithFourChildren);
        foreignDao.create(entityWithFourChildren.children);
        reset(connectionSource);

        // when
        Lazy lazy = lazyDao.queryForId(entityWithFourChildren.id);
        Collection<Foreign> children = lazy.children;

        // then
        verify(connectionSource, times(1)).getReadOnlyConnection(any());
        verify(connectionSource, times(1)).releaseConnection(any());
        assertEquals(4, children.size());
    }

    @Test
    public void testOrmlite() throws SQLException {
        //given
        connectionSource = spy(connectionSource);
        Dao<LazyOrmlite, Integer> lazyDao = createDao(LazyOrmlite.class, true);
        Dao<ForeignOrmlite, Integer> foreignDao = createDao(ForeignOrmlite.class, true);;
        LazyOrmlite entityWithFourChildren = of(new ForeignOrmlite(), new ForeignOrmlite(), new ForeignOrmlite(), new ForeignOrmlite());
        lazyDao.create(entityWithFourChildren);
        foreignDao.create(entityWithFourChildren.children);
        reset(connectionSource);

        // when
        LazyOrmlite lazy = lazyDao.queryForId(entityWithFourChildren.id);
        Collection<ForeignOrmlite> children = lazy.children;

        // then
        verify(connectionSource, times(1)).getReadOnlyConnection(any());
        verify(connectionSource, times(1)).releaseConnection(any());
        assertEquals(4, children.size());
    }

    private Lazy of(Foreign... foreign) {
        Lazy result = new Lazy();
        Arrays.stream(foreign).forEach(f -> f.lazy = result);
        result.children = Arrays.asList(foreign);
        return result;
    }

    private LazyOrmlite of(ForeignOrmlite... foreign) {
        LazyOrmlite result = new LazyOrmlite();
        Arrays.stream(foreign).forEach(f -> f.lazy = result);
        result.children = Arrays.asList(foreign);
        return result;
    }

    @Entity
    private static class Lazy {
        @Id
        @GeneratedValue
        int id;
        @OneToMany
        Collection<Foreign> children;
    }

    @Entity
    private static class Foreign {
        @Id
        @GeneratedValue
        int id;
        @ManyToOne
        Lazy lazy;
    }

    @DatabaseTable(tableName = "lazy_ormlite")
    private static class LazyOrmlite implements Serializable {
        @DatabaseField(generatedId = true)
        int id;

        @ForeignCollectionField(foreignFieldName = "lazy")
        Collection<ForeignOrmlite> children;
    }

    @DatabaseTable(tableName = "foreign_ormlite")
    private static class ForeignOrmlite implements Serializable {
        @DatabaseField(generatedId = true)
        int id;
        @DatabaseField(foreign = true, foreignColumnName = "id")
        LazyOrmlite lazy;
    }
}
