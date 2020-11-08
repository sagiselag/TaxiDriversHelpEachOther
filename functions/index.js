// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.myStorageFunction = functions
    .region('europe-west3')
    .storage
    .object()
    .onFinalize((object) => {
      // ...
    });

// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into Cloud Firestore using the Firebase Admin SDK.
  const writeResult = await admin.firestore().collection('messages').add({original: original});
  // Send back a message that we've succesfully written the message
  res.json({result: `Message with ID: ${writeResult.id} added.`});
});

// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
    .onCreate((snap, context) => {
      // Grab the current value of what was written to Cloud Firestore.
      const original = snap.data().original;

      // Access the parameter `{documentId}` with `context.params`
      console.log('Uppercasing', context.params.documentId, original);
      
      const uppercase = original.toUpperCase();
      
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to Cloud Firestore.
      // Setting an 'uppercase' field in Cloud Firestore document returns a Promise.
      return snap.ref.set({uppercase}, {merge: true});
    });
	
var db = admin.firestore();

// update delay time for today delayed shuttles	which will be relevant in less than 10 minutes
exports.scheduledFunctionForShuttlesWhichWouldBeRelevantInTenMin = functions.pubsub.schedule('every 1 minutes').onRun((context) => {
  console.log('This will be run every 1 minutes!');
  
  return db.collection("todayDelayedShuttles").where("delayInMinutes", "<=", 70).get()
  .then(function(querySnapshot) {
    querySnapshot.forEach(function(doc) {
      // doc.data() is never undefined for query doc snapshots
	  var docItem = doc.data();
      console.log(doc.id, " => ", doc.data());
	  var delayVal = doc.data().delayInMinutes;
	  var idTodelete = doc.id;
	  if(delayVal < 60){
		  // Add a new document with a generated id.
			db.collection("immediateShuttles").add(docItem);
			db.collection("todayDelayedShuttles").doc(doc.id).delete();		
	  }
	  else{
	  // decrement the "delayInMinutes" field of the delayedShuttle
		var newDelayVal = doc.data().delayInMinutes - 1;		
		db.collection('todayDelayedShuttles').doc(doc.id).update({
			delayInMinutes: newDelayVal
		});
	  }
	  return null;
	});
	return null;
  });
});

// update delay time for today delayed shuttles	which will be relevant in less than 100 minutes but bigger than 10
exports.scheduledFunctionForShuttlesInLessThanHundredMin = functions.pubsub.schedule('every 10 minutes').onRun((context) =>
                {
                    console.log('This will be run every 10 minutes!');
					// for delayed shuttles
                    return db.collection("todayDelayedShuttles").where("delayInMinutes", "<=", 160).where("delayInMinutes", ">", 70).get().then(function(querySnapshot)
                    {
                            querySnapshot.forEach(function(doc)
                            {
                                // doc.data() is never undefined for query doc snapshots
                                console.log(doc.id, " => ", doc.data());

                                    // decrement the "delayInMinutes" field of the delayedShuttle
                                    var newDelayVal = doc.data().delayInMinutes - 10;
                                    db.collection('todayDelayedShuttles').doc(doc.id).update(
                                            {
                                                delayInMinutes: newDelayVal
                                            });

                                    return null;
                            });
                            return null;
                    });
                });
				
const immediateDelay = 15;				
// initialize and update delay time for today immediate shuttles
exports.scheduledFunctionForImmediateShuttles = functions.pubsub.schedule('every 1 minutes').onRun((context) =>
                {
                    console.log('This will be run every 1 minutes!');				
					return !db.collection("immediateShuttles").get().then(function(querySnapshot)
                    {
                            querySnapshot.forEach(function(doc)
                            {
                                // doc.data() is never undefined for query doc snapshots
								var docItem = doc.data();
								
								if(docItem.delayInMinutes > 0){
									console.log("update delayInMinutes for ", doc.id);
									var newDelayVal = docItem.delayInMinutes - 1;
									db.collection('immediateShuttles').doc(doc.id).update(
                                            {
                                                delayInMinutes: newDelayVal
                                            });
								}
								else if(docItem.delayInMinutes <= 0){
									// move shuttle to expired shuttles
									db.collection("expiredShuttles").add(docItem);
									db.collection("immediateShuttles").doc(doc.id).delete();		
								}
								else{
									console.log("initialize delayInMinutes for ", doc.id);
										// initialize the "delayInMinutes" field of the immediateShuttle
										db.collection('immediateShuttles').doc(doc.id).update(
												{
													delayInMinutes: immediateDelay
												});
								}
								
                                    return null;
                            });
                            return null;
                    });
                });				

// update delay time for today delayed shuttles	which will be relevant in more than 100 minutes
exports.scheduledFunctionForShuttlesInMoreThanHundredMin = functions.pubsub.schedule('every 100 minutes').onRun((context) => 
{
  console.log('This will be run every 100 minutes!');
  return db.collection("todayDelayedShuttles").where("delayInMinutes", ">", 160).get()
  .then(function(querySnapshot) {
            querySnapshot.forEach(function(doc)
            {
                // doc.data() is never undefined for query doc snapshots
                console.log(doc.id, " => ", doc.data());
                // decrement the "delayInMinutes" field of the delayedShuttle
                var newDelayVal = doc.data().delayInMinutes - 100;
                db.collection('todayDelayedShuttles').doc(doc.id).update(
				{
                        delayInMinutes:newDelayVal
				});
            });
            return null;
        });
});


// update today shuttles list every day
exports.scheduledFunctionForMovingTomorrowIntoToday = functions.pubsub.schedule('0 0 * * *')
  .timeZone('Asia/Jerusalem').onRun((context) => 
{
	console.log('This will be run every day at 00:00!');  
		
	  return db.collection("tomorrowDelayedShuttles").get()
	  .then(function(querySnapshot) {
				querySnapshot.forEach(function(doc)
				{
					// doc.data() is never undefined for query doc snapshots
					console.log(doc.id, " => ", doc.data());
					var docItem = doc.data();
					// Add a new document with a generated id.					
					var newDelayVal = (doc.data().shuttleTime.split(":")[0] * 60) + (doc.data().shuttleTime.split(":")[1]*1);
					console.log("newDelayVal = ", newDelayVal);										
					// Add a new document with a generated id.
					db.collection("todayDelayedShuttles").add({
						docItem, 
						delayInMinutes:newDelayVal
						});
						
					db.collection("tomorrowDelayedShuttles").doc(doc.id).delete();		
				return null;
				});
		return null;
	  });
});


// update tomorrow shuttles list every day
exports.scheduledFunctionForMovingDelayedShuttlesIntoTomorrow = functions.pubsub.schedule('0 0 * * *')
  .timeZone('Asia/Jerusalem').onRun((context) => 
{
		console.log('This will be run every day at 00:00!');
  	
		var currentUtcTime = new Date(); // This is in UTC
		var tomorrow = new Date(currentUtcTime);
		tomorrow.setDate(tomorrow.getDate() + 1);

		// Converts the UTC time to a locale specific format, including adjusting for timezone.
		var tomorrowDateTimeJerusalemTimeZone = new Date(tomorrow.toLocaleString('en-US', { timeZone: 'Asia/Jerusalem' }));
		var dd = tomorrowDateTimeJerusalemTimeZone.getDate();
		var mm = tomorrowDateTimeJerusalemTimeZone.getMonth() + 1;
		var yyyy = tomorrowDateTimeJerusalemTimeZone.getFullYear();

        if (dd < 10) { 
            dd = '0' + dd; 
        } 
        if (mm < 10) { 
            mm = '0' + mm; 
        } 
        var tomorrowStr = dd + '/' + mm + '/' + yyyy; 
		console.log('tomorrowStr: ' + tomorrowStr);
  
	  return db.collection("delayedShuttles").where("shuttleDate", "==", tomorrowStr).get()
	  .then(function(querySnapshot) {
				querySnapshot.forEach(function(doc)
				{
					// doc.data() is never undefined for query doc snapshots
					console.log(doc.id, " => ", doc.data());												
					var docItem = doc.data();					
					db.collection("tomorrowDelayedShuttles").add(docItem);	
					db.collection("delayedShuttles").doc(doc.id).delete();		
				return null;
				});
		return null;
	  });
  
});