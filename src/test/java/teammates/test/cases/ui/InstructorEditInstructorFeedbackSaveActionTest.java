package teammates.test.cases.ui;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackQuestionAttributes;
import teammates.common.datatransfer.FeedbackResponseAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.exception.NullPostParameterException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.storage.api.FeedbackQuestionsDb;
import teammates.storage.api.FeedbackResponsesDb;
import teammates.ui.controller.InstructorEditInstructorFeedbackSaveAction;
import teammates.ui.controller.RedirectResult;

public class InstructorEditInstructorFeedbackSaveActionTest extends BaseActionTest {

    private static DataBundle dataBundle;
    
    @BeforeClass
    public static void classSetUp() throws Exception {
        printTestClassHeader();
        dataBundle = loadDataBundle("/InstructorEditInstructorFeedbackPageTest.json");
        removeAndRestoreDatastoreFromJson("/InstructorEditInstructorFeedbackPageTest.json");
        
        uri = Const.ActionURIs.INSTRUCTOR_EDIT_INSTRUCTOR_FEEDBACK_SAVE;
    }

    @Test
    public void testExecuteAndPostProcess() throws Exception {
        testModifyResponses();
        testIncorrectParameters();
        testDifferentPrivileges();
        testSubmitResponseForInvalidQuestion();
        testClosedSession();
    }
    
    private void testModifyResponses() throws Exception {
        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq = fqDb.getFeedbackQuestion("First feedback session", "IEIFPTCourse", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1ForQ1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);

        InstructorAttributes instructor = dataBundle.instructors.get("IEIFPTCourseinstr");
        InstructorEditInstructorFeedbackSaveAction editInstructorFSAction;
        RedirectResult redirectResult;
        String moderatedInstructorEmail = "IEIFPTCoursehelper1@gmail.tmt";
        String[] submissionParams;

        gaeSimulation.loginAsInstructor(instructor.googleId);
        
        ______TS("edit existing answer");
        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };
        
        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();
        
        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCoursehelper1%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=First+feedback+session",redirectResult.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));

        ______TS("deleted response");
        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "",
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };
        
        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();

        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCoursehelper1%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=First+feedback+session", redirectResult.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));

        ______TS("skipped question");
        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "" ,
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();
        
        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCoursehelper1%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=First+feedback+session", redirectResult.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));

        ______TS("new response");
        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "New " + fr.getResponseDetails().getAnswerString(), 
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();

        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCoursehelper1%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=First+feedback+session", redirectResult.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));
    }

    private void testIncorrectParameters() throws Exception {
        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq = fqDb.getFeedbackQuestion("First feedback session", "IEIFPTCourse", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1ForQ1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);

        InstructorAttributes instructor = dataBundle.instructors.get("IEIFPTCourseinstr");
        InstructorEditInstructorFeedbackSaveAction editInstructorFSAction;
        String moderatedInstructorEmail = "IEIFPTCoursehelper1@gmail.tmt";
        String[] submissionParams;

        gaeSimulation.loginAsInstructor(instructor.googleId);
        
        ______TS("Unsuccessful case: test empty feedback session name parameter");
        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, dataBundle.feedbackResponses.get("response1ForQ1").courseId,
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullPostParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER, Const.ParamsNames.FEEDBACK_SESSION_NAME), 
                         e.getMessage());
        }

        ______TS("Unsuccessful case: test empty course id parameter");
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_SESSION_NAME, dataBundle.feedbackResponses.get("response1ForQ1").feedbackSessionName, 
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };
        
        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullPostParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER, Const.ParamsNames.COURSE_ID), 
                         e.getMessage());
        }

        ______TS("Unsuccessful case: test no moderated student parameter");
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", "",
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
        };
        
        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullPostParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER, 
                                       Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON), 
                         e.getMessage());
        }
    }
    
    private void testDifferentPrivileges() throws Exception {
        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq = fqDb.getFeedbackQuestion("First feedback session", "IEIFPTCourse", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr;

        InstructorAttributes instructor;
        InstructorEditInstructorFeedbackSaveAction editInstructorFSAction;
        RedirectResult redirectResult;
        String moderatedInstructorEmail;
        String[] submissionParams;

        ______TS("Unsuccessful case: insufficient privileges");
        fr = dataBundle.feedbackResponses.get("response1ForQ1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);
        
        instructor = dataBundle.instructors.get("IEIFPTCoursehelper1");
        moderatedInstructorEmail = "IEIFPTCoursehelper1@gmail.tmt";
        
        gaeSimulation.loginAsInstructor(instructor.googleId);
        
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };
        
        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException();
        } catch (UnauthorizedAccessException e) {
            assertEquals("Feedback session [First feedback session] is not accessible to " +
                         "instructor [" + instructor.email + "] for privilege [canmodifysession]", e.getMessage());
        }

        ______TS("Successful case: Course Instructor edit Course Instructor");
        fr = dataBundle.feedbackResponses.get("response2ForQ1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);
        
        instructor = dataBundle.instructors.get("IEIFPTCourseinstr");
        moderatedInstructorEmail = "IEIFPTCourseintr@gmail.tmt";
        
        gaeSimulation.loginAsInstructor(instructor.googleId);
        
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };
        
        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();

        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCourseintr%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=First+feedback+session",redirectResult.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));
    }
    
    private void testSubmitResponseForInvalidQuestion() throws Exception {
        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq;

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr;

        InstructorAttributes instructor = dataBundle.instructors.get("IEIFPTCourseinstr");
        InstructorEditInstructorFeedbackSaveAction editInstructorFSAction;
        String moderatedInstructorEmail = "IEIFPTCoursehelper1@gmail.tmt";
        String[] submissionParams;

        gaeSimulation.loginAsInstructor(instructor.googleId);

        ______TS("Failure case: submit response for question in session, but should not be editable by instructor (unable to see recipient)");
        fq = fqDb.getFeedbackQuestion("First feedback session", "IEIFPTCourse", 4);
        assertNotNull("Feedback question not found in database", fq);
        
        fr = dataBundle.feedbackResponses.get("response1ForQ4");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);
        
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException("Did not detect that this instructor cannot access this particular question.");
        } catch (UnauthorizedAccessException e) {
            assertEquals("Feedback session [First feedback session] question [" + fr.feedbackQuestionId + "] " +
                         "is not accessible to instructor [" + instructor.email + "]", e.getMessage());
        }

        ______TS("Failure case: submit response for question in session, but should not be editable by instructor (unable to see giver)");
        fq = fqDb.getFeedbackQuestion("First feedback session", "IEIFPTCourse", 5);
        assertNotNull("Feedback question not found in database", fq);
        
        fr = dataBundle.feedbackResponses.get("response1ForQ5");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);
        
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        try {
            editInstructorFSAction = getAction(submissionParams);
            editInstructorFSAction.executeAndPostProcess();
            signalFailureToDetectException("Did not detect that this instructor cannot access this particular question.");
        } catch (UnauthorizedAccessException e) {
            assertEquals("Feedback session [First feedback session] question [" + fr.feedbackQuestionId + "] " +
                         "is not accessible to instructor [" + instructor.email + "]", e.getMessage());
        }
    }

    private void testClosedSession() throws Exception {
        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq = fqDb.getFeedbackQuestion("Closed feedback session", "IEIFPTCourse", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1ForQ1InClosedSession");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail);
        assertNotNull("Feedback response not found in database", fr);

        InstructorAttributes instructor = dataBundle.instructors.get("IEIFPTCourseinstr");
        InstructorEditInstructorFeedbackSaveAction editInstructorFSAction;
        RedirectResult redirectResult;
        String moderatedInstructorEmail = "IEIFPTCourseintr@gmail.tmt";
        String[] submissionParams;

        gaeSimulation.loginAsInstructor(instructor.googleId);

        ______TS("Success case: modifying responses in closed session");
        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, moderatedInstructorEmail
        };

        editInstructorFSAction = getAction(submissionParams);
        redirectResult = (RedirectResult) editInstructorFSAction.executeAndPostProcess();
        
        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        assertEquals("/page/instructorEditInstructorFeedbackPage?error=false" +
                     "&moderatedperson=IEIFPTCourseintr%40gmail.tmt&user=IEIFPTCourseinstr" +
                     "&courseid=IEIFPTCourse&fsname=Closed+feedback+session",redirectResult.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giverEmail, fr.recipientEmail));
    }
    
    private InstructorEditInstructorFeedbackSaveAction getAction(String... params) throws Exception {
        return (InstructorEditInstructorFeedbackSaveAction) (gaeSimulation.getActionObject(uri, params));
    }
}
