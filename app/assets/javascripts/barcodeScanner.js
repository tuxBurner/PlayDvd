/**
 *
 * User: tuxburner
 * Date: 2/11/13
 * Time: 8:16 PM
 * This is used for capturing the barcode from a webcam
 * TODO: http://www.smartjava.org/content/face-detection-using-html5-javascript-webrtc-websockets-jetty-and-javacvopencv use not flash :)
 */

$(function () {

  var ws = new WebSocket(wsBarcodeUrl);

  // initialize the webcam
  ws.onopen = function () {
    $("#camera").webcam({
      width: 320,
      height: 240,
      mode: "callback",
      swffile: "/assets/jquery-webcam/jscam.swf",
      onSave: saveCB,
      onCapture: function () {
        webcam.save();
      },onTick: function(remain) {
        if (0 == remain) {
          jQuery("#barcodeStatus").text("Checking code.");
        } else {
          jQuery("#barcodeStatus").text(remain + " seconds remaining...");
        }
      }
    });
  }

  ws.onmessage = function(e) {
    if(e.data == "error") {
      jQuery("#barcodeStatus").text("Error trying again.");
      webcam.capture(1);
    } else {
     $('#eanNr').val(e.data);
     closeDialog();
    }
  }

  var canvas = document.createElement("canvas");
  canvas.setAttribute('width', 320);
  canvas.setAttribute('height', 240);

  if (canvas.toDataURL) {

    var pos = 0, ctx = null, saveCB, image = [];

    ctx = canvas.getContext("2d");

    image = ctx.getImageData(0, 0, 320, 240);

    saveCB = function(data) {

      var col = data.split(";");
      var img = image;

      for(var i = 0; i < 320; i++) {
        var tmp = parseInt(col[i]);
        img.data[pos + 0] = (tmp >> 16) & 0xff;
        img.data[pos + 1] = (tmp >> 8) & 0xff;
        img.data[pos + 2] = tmp & 0xff;
        img.data[pos + 3] = 0xff;
        pos+= 4;
      }

      if (pos >= 4 * 320 * 240) {
        ctx.putImageData(img, 0, 0);
        ws.send(canvas.toDataURL('image/jpeg', 1.0));
        pos = 0;
      }
    };
  }

});

/**
 * Triggers the barcode event
 */
var barcodeStartCaputring = function () {
  webcam.capture(1);
}

