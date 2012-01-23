// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;
import java.net.Socket;

import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;

public class SocketCatchingResponder implements Responder, SocketDoner, ResponsePuppeteer {
  private int ticketNumber;
  private SocketDealer dealer;
  private Socket socket;
  private ResponseSender sender;
  private PuppetResponse response;

  public Response makeResponse(FitNesseContext context, Request request) {
    dealer = context.socketDealer;
    ticketNumber = Integer.parseInt(request.getInput("ticket").toString());
    response = new PuppetResponse(this);
    return response;
  }

  public void readyToSend(ResponseSender sender) {
    socket = sender.getSocket();
    this.sender = sender;
    try {
      if (dealer.isWaiting(ticketNumber))
        dealer.dealSocketTo(ticketNumber, this);
      else {
      	String errorMessage = "There are no clients waiting for a socket with ticketNumber " + ticketNumber;
      	FitProtocol.writeData(errorMessage, socket.getOutputStream());
      	response.setStatus(404);
      	sender.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Socket donateSocket() {
    return socket;
  }

  public void finishedWithSocket() {
    sender.close();
  }
}
