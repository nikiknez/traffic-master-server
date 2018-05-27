var selectedCam = null;
var streetSelectMode = false;

function IpCamera(name, location, ipAddr) {
    this.name = name;
    this.ipAddr = ipAddr;
    this.myStreets = [];
    this.enterStreetSelectMode = enterStreetSelectMode;
    this.exitStreetSelectMode = exitStreetSelectMode;
    var cam = this;

    var div = document.createElement("div");
    div.innerHTML = "<h3>" + name + "</h3>";

    cam.img = new Image(); //document.createElement("img");
//    cam.img.crossOrigin = "anonymous";
    cam.img.src = ipAddr;
    $(cam.img).css("max-height", 200);
    div.appendChild(cam.img);
    $(div).append("<br/><br/>");

    var pauseButton = document.createElement("button");
    $(pauseButton).addClass("btn btn-default");
    pauseButton.innerHTML = "Pauziraj";
    div.appendChild(pauseButton);

    var configButton = document.createElement("button");
    $(configButton).addClass("btn btn-default hidden");
    configButton.innerHTML = "Konfigurisi kameru";
    div.appendChild(configButton);
    $(configButton).click(function () {
        cam.enterStreetSelectMode();
    });

    cam.marker = new google.maps.Marker({
        position: location,
        map: map,
        icon: 'icons/camera.png',
        title: name
    });
    cam.marker.addListener('click', function () {
        if (drawingMode || streetSelectMode) {
            return;
        }
        if (camSelectMode) {
            cam.myStreets.push(selectedStreet.id);
            selectedStreet.exitCamSelectMode(cam);
            return;
        }
        if (!isInfoWOpen) {
            cam.openInfoW();
        } else {
            cam.closeInfoW();
        }
    });

    cam.infowindow = new google.maps.InfoWindow();
    cam.infowindow.setContent(div);
    cam.infowindow.addListener('closeclick', function () {
        clearInterval(cam.refreshInterval);
        isInfoWOpen = false;
        isPaused = false;
    });

    var isPaused = false;
    $(pauseButton).click(function () {
        if (streetSelectMode) {
            return;
        }
        if (!isPaused) {
            clearInterval(cam.refreshInterval);
            cam.refreshInterval = null;
            pauseButton.innerHTML = "Nastavi";
            if (currentUser === "admin" && cam.myStreets.length > 0) {
                $(configButton).removeClass("hidden");
                var p = {url: ipAddr};
                console.log("sending request");
                $.post("DownloadFrameServlet", $.param(p), function (responseText) {
                    var b64 = btoa(unescape(encodeURIComponent(responseText)));
                    console.log(b64);
//                    cam.img.src = "data:image/jpg;base64," + b64;
                });
            }
            isPaused = true;
        } else {
            cam.updateImage();
            cam.refreshInterval = setInterval(cam.updateImage, 200);
            pauseButton.innerHTML = "Pauziraj";
            $(configButton).addClass("hidden");
            isPaused = false;
        }
    });

    this.updateImage = function () {
        var r = Math.random();
        var src = cam.ipAddr + "?uniq=" + r;
        cam.img.src = src;
    };

    var isInfoWOpen = false;
    this.openInfoW = function () {
        clearInterval(cam.refreshInterval);
        cam.updateImage();
        cam.infowindow.open(map, cam.marker);
        cam.refreshInterval = setInterval(cam.updateImage, 500);
        pauseButton.innerHTML = "Pauziraj";
        $(configButton).addClass("hidden");
        isInfoWOpen = true;
        isPaused = false;
    };
    this.closeInfoW = function () {
        cam.infowindow.close();
        clearInterval(cam.refreshInterval);
        isInfoWOpen = false;
        isPaused = false;
    };

    this.getImageSrc = function () {
        return this.img;
    };
    this.getImageDim = function () {
        return {width: this.img.naturalWidth, height: this.img.naturalHeight};
    };

}


function FileCamera(name, location, file) {
    this.name = name;
    this.file = file;
    this.myStreets = [];
    this.enterStreetSelectMode = enterStreetSelectMode;
    this.exitStreetSelectMode = exitStreetSelectMode;
    var cam = this;

    var div = document.createElement("div");
    div.innerHTML = "<h3>" + name + "</h3>";

    var video = document.createElement("video");
    video.autoplay = true;
    video.muted = true;
    video.loop = true;
    video.src = file;
//    var URL = window.URL || window.webkitURL;
//    video.src = URL.createObjectURL(file);
    $(video).css("max-height", 200);
    div.appendChild(video);
    $(div).append("<br><br>");

    var pauseButton = document.createElement("button");
    $(pauseButton).addClass("btn btn-default");
    pauseButton.innerHTML = "Pauziraj";
    div.appendChild(pauseButton);

    var configButton = document.createElement("button");
    $(configButton).addClass("btn btn-default hidden");
    configButton.innerHTML = "Konfigurisi kameru";
    div.appendChild(configButton);
    $(configButton).click(function () {
        cam.enterStreetSelectMode();
    });

    var marker = new google.maps.Marker({
        position: location,
        map: map,
        icon: 'icons/camera.png',
        title: name
    });
    marker.addListener('click', function () {
        if (drawingMode || streetSelectMode) {
            return;
        }
        if (camSelectMode) {
            cam.myStreets.push(selectedStreet.id);
            selectedStreet.exitCamSelectMode(cam);
            return;
        }
        if (!isInfoWOpen) {
            cam.openInfoW();
        } else {
            cam.closeInfoW();
        }
    });

    var infowindow = new google.maps.InfoWindow();
    infowindow.setContent(div);
    infowindow.addListener('closeclick', function () {
        isInfoWOpen = false;
        isPaused = false;
    });

    var isPaused = false;
    $(pauseButton).click(function () {
        if (streetSelectMode) {
            return;
        }
        if (!isPaused) {
            pauseButton.innerHTML = "Nastavi";
            if (currentUser === "admin" && cam.myStreets.length > 0) {
                $(configButton).removeClass("hidden");
            }
            video.pause();
            isPaused = true;
        } else {
            pauseButton.innerHTML = "Pauziraj";
            $(configButton).addClass("hidden");
            video.play();
            isPaused = false;
        }
    });

    var isInfoWOpen = false;
    this.openInfoW = function () {
        infowindow.open(map, marker);
        pauseButton.innerHTML = "Pauziraj";
        $(configButton).addClass("hidden");
        video.play();
        isInfoWOpen = true;
        isPaused = false;
    };
    this.closeInfoW = function () {
        infowindow.close();
        isInfoWOpen = false;
        isPaused = false;
    };

    this.getImageSrc = function () {
        return video;
    };
    this.getImageDim = function () {
        return {width: video.videoWidth, height: video.videoHeight};
    };
    this.getVideoTime = function() {
      return video.currentTime;  
    };
}

function enterStreetSelectMode() {
    streetSelectMode = true;
    map.setOptions({disableDefaultUI: true});
    $("#centerControlDiv").hide();
    $("#userControlDiv").hide();
    $("#info5").removeClass("hidden");
    $("#streetSelectDiv").removeClass("hidden");
    selectedCam = this;
    var i;
    for (i = 0; i < cameras.length; i++) {
        if (cameras[i] !== selectedCam) {
            cameras[i].closeInfoW();
        }
    }
}

function exitStreetSelectMode(street) {
    streetSelectMode = false;
    map.setOptions({disableDefaultUI: false});
    $("#centerControlDiv").show();
    $("#userControlDiv").show();
    $("#info5").addClass("hidden");
    $("#streetSelectDiv").addClass("hidden");
    if (street) {
        new CamConfig(selectedCam, street);
    }
    selectedCam = null;
}
