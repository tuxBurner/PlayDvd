/**
 *
 * User: tuxburner
 * Date: 2/11/13
 * Time: 8:16 PM
 * This is used for capturing the barcode from a webcam
 * TODO: http://www.smartjava.org/content/face-detection-using-html5-javascript-webrtc-websockets-jetty-and-javacvopencv use not flash :)
 */

var searchAfterScan = false;
var nativeWebcam = false;
var canvas = document.getElementById('bccanvas');
canvas.setAttribute('width', 320);
canvas.setAttribute('height', 240);

var ctx = canvas.getContext('2d');


// needed for native
var nativeVideo = null;
var nativeMediaStream = null;

// needed for not native
var pos = 0;
var image = ctx.getImageData(0, 0, 320, 240);

// websocket
var ws = null;

$(function () {
  // open the websocket
  ws = new WebSocket(wsBarcodeUrl);
});

/**
 * Initializes the webcam when the socket to the backend is opened :)
 */
ws.onopen = function () {
  nativeWebcam = supportsNativeWebcam();
  // for chrome opera and firefox use the native webcam support
  if (nativeWebcam == true) {
    startNativeWebcam();
  } else {
    // use flash
    startWebcamFallback();
  }
}

/**
 * starts the native webcam support
 */
var startNativeWebcam = function () {
  $('#cameraWrapper').append('<video autoplay width="320" height="240"></video>');
  nativeVideo = document.querySelector('video');

  // this is needed for crossbrowser support
  navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia ||
    navigator.mozGetUserMedia || navigator.msGetUserMedia;
  window.URL = window.URL || window.webkitURL;

  // start the webcam stream
  navigator.getUserMedia({video: true}, function (stream) {
    nativeVideo.src = window.URL.createObjectURL(stream);
    nativeMediaStream=stream;
    sizeNativeCanvas();
  }, function () {
    startWebcamFallback();
  });
}

/**
 * Sizes the canvas to the video so it doesn't get crippled
 */
function sizeNativeCanvas(){
  setTimeout(function(){
    canvas.width=nativeVideo.videoWidth;
    canvas.height=nativeVideo.videoHeight;
  },100);
}

/**
 * Sends the image data of the native webcam
 */
var sendNativeBC = function() {
  jQuery("#barcodeStatus").text("Checking code.");
  ctx.drawImage(nativeVideo, 0, 0);
  ws.send(canvas.toDataURL('image/jpeg', 1.0));
}

/**
 * Starts the fallback with flash
 */
var startWebcamFallback = function () {
  $('#cameraWrapper').append('<div id="camera"></div>');
  $("#camera").webcam({
    width: 320,
    height: 240,
    mode: "callback",
    swffile: "/assets/jquery-webcam/jscam.swf",
    onSave: sendBCFallback,
    onLoad: function () {
      var cams = webcam.getCameraList();
      if (cams.length > 1) {
        for (var i in cams) {
          var selected = (i == 0) ? ' selected="selected"' : '';
          jQuery("#cams").append('<option value="' + i + '"' + selected + '>' + cams[i] + '</option>');
        }
        $('#cams').change(function () {
          webcam.setCamera($(this).val());
        });
        $('#camsWrapper').show();
      }
    },
    onCapture: function () {
      webcam.save();
    }, onTick: function (remain) {
      if (0 == remain) {
        jQuery("#barcodeStatus").text("Checking code.");
      } else {
        jQuery("#barcodeStatus").text(remain + " seconds remaining...");
      }
    }
  });
}

/**
 * Sends the image when the flashi fallback method is used
 * @param data
 */
  var sendBCFallback = function (data) {
    var col = data.split(";");
    var img = image;

    for (var i = 0; i < 320; i++) {
      var tmp = parseInt(col[i]);
      img.data[pos + 0] = (tmp >> 16) & 0xff;
      img.data[pos + 1] = (tmp >> 8) & 0xff;
      img.data[pos + 2] = tmp & 0xff;
      img.data[pos + 3] = 0xff;
      pos += 4;
    }

    if (pos >= 4 * 320 * 240) {
      ctx.putImageData(img, 0, 0);
      ws.send(canvas.toDataURL('image/jpeg', 1.0));
      pos = 0;
    }
  };


/**
 * When we recieve a message over the websocket we have to take care for ite
 * @param e
 */
ws.onmessage = function (e) {
  if (e.data == "error") {
    jQuery("#barcodeStatus").text("Error trying again.");
    if(nativeWebcam == true) {
      jQuery("#barcodeStatus").text("Wait 1 second");
      window.setTimeout("sendNativeBC()",1000);
    } else {
      webcam.capture(1);
    }
  } else {
    if (searchAfterScan == true) {
      openAmazonLookUp(e.data);
    } else {
      closeDialog();
    }

  }
}


/**
 * Stops the webcam access from the browser
 */
var stopVideoCapture= function () {
  if(nativeWebcam == true) {
    nativeVideo.pause();
    nativeMediaStream.stop();
  }
};

/**
 * Triggers the barcode event
 * @param search if true the scanned code will be taken for searching @ amazon
 */
var barcodeStartCaputring = function (search) {
  searchAfterScan = search;
  if(nativeWebcam == true) {
    sendNativeBC();
  }  else {
    webcam.capture(1);
  }
}

var supportsNativeWebcam = function () {
  // Note: Opera is unprefixed.
  return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||
    navigator.mozGetUserMedia || navigator.msGetUserMedia);
}