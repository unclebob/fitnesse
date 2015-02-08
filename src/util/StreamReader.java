// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import fitnesse.slim.SlimVersion;

public class StreamReader {
  private InputStream input;
  private State state;

  ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
  OutputStream output;

  private int readGoal;
  private int readStatus;
  
  // timeout limit in milli seconds
  // 0 = wait forever, never timeout
  private int timeoutLimit = 0;

  private boolean eof = false;
  private boolean isTimeout = false;
  private int retryCounter=0;
  private int sleepStep = 10;
  
  private byte[] boundary;
  private int boundaryLength;
  private int matchingBoundaryIndex;
  private byte[] matchedBoundaryBytes;

  private long bytesConsumed;

  public StreamReader(InputStream input) {
    this.input = input;
  }

  public static void sendSlimMessage(BufferedOutputStream writer, String message) throws IOException {
	byte[] msgChars = message.getBytes(SlimVersion.CHARENCODING);
	byte[] msgLength = String.format(SlimVersion.LENGTH_FORMAT, msgChars.length).getBytes(SlimVersion.CHARENCODING);
    writer.write(msgLength, 0, msgLength.length);
	writer.write(msgChars, 0, msgChars.length);
	writer.flush();
  }

  public static void sendSlimHeader(BufferedOutputStream writer, String header) throws IOException {
	// The Header has no length information as prefix
	byte[] msgChars = header.getBytes(SlimVersion.CHARENCODING);
	writer.write(msgChars, 0, msgChars.length);
	writer.flush();
  }
 
  
  public void close() throws IOException {
    input.close();
  }
  
  public void setTimeoutLimit(int timeout)  {
    timeoutLimit = timeout;
  }
 
  public int timeoutLimit()  {
	    return timeoutLimit;
	  }
  public boolean isTimeout(){
	  return isTimeout;
  }
  
  public String readLine() throws IOException {
    return bytesToString(readLineBytes());
  }

  public byte[] readLineBytes() throws IOException {
    state = READLINE_STATE;
    return preformRead();
  }

  public String read(int count) throws IOException {
    return bytesToString(readBytes(count));
  }

  public byte[] readBytes(int count) throws IOException {
    readGoal = count;
    readStatus = 0;
    state = READCOUNT_STATE;
    return preformRead();
  }

  public void copyBytes(int count, OutputStream output) throws IOException {
    readGoal = count;
    state = READCOUNT_STATE;
    performCopy(output);
  }

  public String readUpTo(String boundary) throws IOException {
    return bytesToString(readBytesUpTo(boundary));
  }

  public byte[] readBytesUpTo(String boundary) throws IOException {
    prepareForReadUpTo(boundary);
    return preformRead();
  }

  private void prepareForReadUpTo(String boundary) {
    this.boundary = boundary.getBytes();
    boundaryLength = this.boundary.length;
    matchedBoundaryBytes = new byte[boundaryLength];
    matchingBoundaryIndex = 0;
    state = READUPTO_STATE;
  }

  public void copyBytesUpTo(String boundary, OutputStream outputStream) throws IOException {
    prepareForReadUpTo(boundary);
    performCopy(outputStream);
  }

  public int byteCount() {
    return byteBuffer.size();
  }

  public byte[] getBufferedBytes() {
    return byteBuffer.toByteArray();
  }

  private byte[] preformRead() throws IOException {
    setReadMode();
    clearBuffer();
    readUntilFinished();
    return getBufferedBytes();
  }

  private void performCopy(OutputStream output) throws IOException {
    setCopyMode(output);
    readUntilFinished();
  }

  private void readUntilFinished() throws IOException {
	  isTimeout = false;
	  
	  if(timeoutLimit >0){
		  retryCounter = timeoutLimit / sleepStep;
	  }
	  else{
		  retryCounter = 0;
	  }
	  while (!state.finished())
		  //TODO remove the true or make it used only for non SSL streams
		  // Note: SSL sockets don't support the input.available() function :(
		  if(timeoutLimit == 0 || input.available() !=0 ){
			state.read(input);
			  
		  }else{
			try {
				Thread.sleep(sleepStep);
			} catch (InterruptedException e) {
				// Ignore
				//e.printStackTrace();
			}
			retryCounter--;
			if (retryCounter <= 0){
			  isTimeout = true;
			  changeState(FINAL_STATE);
			}
		  }
  }

  private void clearBuffer() {
    byteBuffer.reset();
  }

  private void setCopyMode(OutputStream output) {
    this.output = output;
  }

  private void setReadMode() {
    output = byteBuffer;
  }

  private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
    return new String(bytes, SlimVersion.CHARENCODING);
  }

  private void changeState(State state) {
    this.state = state;
  }

  public boolean isEof() {
    return eof;
  }

  public long numberOfBytesConsumed() {
    return bytesConsumed;
  }

  public void resetNumberOfBytesConsumed() {
    bytesConsumed = 0;
  }

  private static abstract class State {
    public void read(InputStream input) throws IOException {
    }

    public boolean finished() {
      return false;
    }
  }

  private final State READLINE_STATE = new State() {
    public void read(InputStream input) throws IOException {
      int b = input.read();
      if (b == -1) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed++;
        if (b == '\n')
          changeState(FINAL_STATE);
        else if (b != '\r')
          output.write((byte) b);
      }
    }
  };

  private final State READCOUNT_STATE = new State() {
    public void read(InputStream input) throws IOException {
      byte[] bytes = new byte[readGoal - readStatus];
      int bytesRead = input.read(bytes);

      if (bytesRead < 0) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed += bytesRead;
        readStatus += bytesRead;
        output.write(bytes, 0, bytesRead);
      }
    }

    public boolean finished() {
      return readStatus >= readGoal;
    }
  };

  private final State READUPTO_STATE = new State() {
    public void read(InputStream input) throws IOException {
      int b = input.read();
      if (b == -1) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed++;
        if (b == boundary[matchingBoundaryIndex]) {
          matchedBoundaryBytes[matchingBoundaryIndex++] = (byte) b;
          if (matchingBoundaryIndex >= boundaryLength)
            changeState(FINAL_STATE);
        } else if (matchingBoundaryIndex == 0)
          output.write((byte) b);
        else {
          output.write(matchedBoundaryBytes, 0, matchingBoundaryIndex);
          matchingBoundaryIndex = 0;
          if (b == boundary[matchingBoundaryIndex])
            matchedBoundaryBytes[matchingBoundaryIndex++] = (byte) b;
          else
            output.write((byte) b);
        }
      }
    }
  };

  private final State FINAL_STATE = new State() {
    public boolean finished() {
      return true;
    }
  };
  
  private int getLengthToRead() throws IOException  {
	    String length = read(SlimVersion.MINIMUM_NUMBER_LENGTH);

	      //Continue to read up to the ":"
	      String next;
	      while (!":".equals(next = read(1)) && !eof && !isTimeout)
   	        length = length + next;

	      if(eof) throw new IOException("Stream Read Failure. Can't read length of message, EOF reached.  Possibly test aborted.  Last things read: " + length);
	      if(isTimeout) throw new IOException("Stream Read Failure. Can't read length of message, Timeout reached.  Possibly test aborted.  Last things read: " + length);
	      
		try {
		  Integer resultLength = Integer.parseInt(length);
	      return resultLength;
	    }
	    catch (NumberFormatException e){
	      throw new IOException("Stream Read Failure. Can't read length of message, not a number.  Possibly test aborted.  Last things read: " + length);
	    }
	  }

  public String getSlimMessage() throws IOException {
    int resultLength = getLengthToRead();
    return  read(resultLength);
  }
}
