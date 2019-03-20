package reposense.git;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import reposense.model.Author;
import reposense.template.GitTestTemplate;
import reposense.util.TestUtil;

public class GitShortlogTest extends GitTestTemplate {

    @Test
    public void getAuthors_validRepoNoDateRange_success() {
        List<Author> expectedAuthorList = new ArrayList<>();
        expectedAuthorList.add(new Author("Eugene Peh"));
        expectedAuthorList.add(new Author("eugenepeh"));
        expectedAuthorList.add(new Author("fakeAuthor"));
        expectedAuthorList.add(new Author("harryggg"));

        List<Author> actualAuthorList = GitShortlog.getAuthors(config);

        Assert.assertEquals(expectedAuthorList.size(), actualAuthorList.size());
        Assert.assertEquals(expectedAuthorList, actualAuthorList);
    }

    @Test
    public void getAuthors_validRepoDateRange_success() {
        List<Author> expectedAuthorList = new ArrayList<>();

        expectedAuthorList.add(new Author("eugenepeh"));

        Date sinceDate = TestUtil.getDate(2018, Calendar.MAY, 5);
        Date untilDate = TestUtil.getDate(2018, Calendar.MAY, 10);

        List<Author> actualAuthorList = GitShortlog.getAuthors(config, sinceDate, untilDate);

        Assert.assertEquals(expectedAuthorList.size(), actualAuthorList.size());
        Assert.assertEquals(expectedAuthorList, actualAuthorList);
    }

    @Test
    public void getAuthors_validRepoDateOutOfRange_success() {
        Date sinceDate = TestUtil.getDate(2018, Calendar.JUNE, 1);
        Date untilDate = TestUtil.getDate(2018, Calendar.JUNE, 20);

        List<Author> actualAuthorList = GitShortlog.getAuthors(config, sinceDate, untilDate);

        Assert.assertTrue(actualAuthorList.isEmpty());
    }
}
