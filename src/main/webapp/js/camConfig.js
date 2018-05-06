
function CamConfig(cam, street) {
    $("#camConfigOverlay").removeClass("hidden");
    $("#camConfigCanvas").removeClass("hidden");
    $("#nextCamConfigButton").removeClass("hidden");
    $("#info2").removeClass("hidden");
    $("#lineLengthInput").addClass("hidden");
    $("#saveCamConfigButton").addClass("hidden");
    $("#prevCamConfigButton").addClass("hidden");
    $("#info3").addClass("hidden");

    var canvas = document.getElementById("camConfigCanvas");
    var img = cam.getImageSrc();
    setToCenter(canvas, cam.getImageDim());
    var w = canvas.width;
    var h = canvas.height;
    var poly = [
        {x: w / 3, y: h / 3},
        {x: 2 * w / 3, y: h / 3},
        {x: 2 * w / 3, y: 2 * h / 3},
        {x: w / 3, y: 2 * h / 3}
    ];
    var c2 = canvas.getContext('2d');
    var loop = setInterval(drawPoly, 40);

    var transformedImg;
    var line = [];
    var loop2;

    var step = 1;

    $("#nextCamConfigButton")[0].onclick = function () {
        clearInterval(loop);
        $.ajax({
            type: "POST",
            url: "ConfigCameraServlet",
            data: createRequestData()
        }).done(function (o) {
            console.log('done ' + o);
            if (o === "ok") {
                nextCamConfigAction();
            }
        });
    };

    function nextCamConfigAction() {
        transformedImg = document.createElement("img");
        transformedImg.onload = function () {
            var dim = {width: transformedImg.naturalWidth,
                height: transformedImg.naturalHeight};
            setToCenter(canvas, dim);
            w = canvas.width;
            h = canvas.height;
            c2 = canvas.getContext('2d');
            if (w > h) {
                line = [{x: w / 3, y: h / 2}, {x: 2 * w / 3, y: h / 2}];
            } else {
                line = [{x: w / 2, y: h / 3}, {x: w / 2, y: 2 * h / 3}];
            }
            loop2 = setInterval(drawLine, 40);
        };
        transformedImg.src = "/temp/transformed.jpg" + "?uniq=" + Math.random();
        step = 2;
        $("#lineLengthInput").removeClass("hidden");
        $("#saveCamConfigButton").removeClass("hidden");
        $("#prevCamConfigButton").removeClass("hidden");
        $("#nextCamConfigButton").addClass("hidden");
        $("#info2").addClass("hidden");
        $("#info3").removeClass("hidden");
    }

    function createRequestData() {
        var p = [
            {x: poly[0].x / w, y: poly[0].y / h},
            {x: poly[1].x / w, y: poly[1].y / h},
            {x: poly[2].x / w, y: poly[2].y / h},
            {x: poly[3].x / w, y: poly[3].y / h}
        ];
        var data = {
            poly: JSON.stringify(p)
        };
        if (cam.getVideoTime) {
            data.videoFileName = cam.file;
            data.videoTime = cam.getVideoTime() * 1000;
        }
        console.log(data);
        return $.param(data);
    }

    $("#prevCamConfigButton")[0].onclick = function () {
        clearInterval(loop2);
        $("#lineLengthInput").addClass("hidden");
        $("#saveCamConfigButton").addClass("hidden");
        $("#prevCamConfigButton").addClass("hidden");
        $("#nextCamConfigButton").removeClass("hidden");
        $("#info2").removeClass("hidden");
        $("#info3").addClass("hidden");
        setToCenter(canvas, cam.getImageDim());
        w = canvas.width;
        h = canvas.height;
        c2 = canvas.getContext('2d');
        loop = setInterval(drawPoly, 40);
        step = 1;
    };

    $("#saveCamConfigButton")[0].onclick = function () {
        var lineLength = $("#lineLengthInput").val().trim();
        
        if (isNaN(lineLength) || lineLength === "") {
            $("#lineLengthInput").val("Nevalidan format");
            return;
        }
        
        clearInterval(loop);
        clearInterval(loop2);

        var l = [
            {x: line[0].x / w, y: line[0].y / h},
            {x: line[1].x / w, y: line[1].y / h}
        ];
        var data = {
            line: JSON.stringify(l),
            lineLength: lineLength,
            streetId: street.id,
            cameraId: cam.id
        };
        $.post("SaveCameraConfigServlet", $.param(data), function (responseText) {
            console.log(responseText);
        });
        console.log(data);
        $("#camConfigOverlay").addClass("hidden");
    };

    $("#cancleCamConfigButton")[0].onclick = function () {
        clearInterval(loop);
        clearInterval(loop2);
        $("#camConfigOverlay").addClass("hidden");
    };

    var draggingCorner = -1;
    var mouseDrag = false;

    $(canvas)[0].onmousedown = function (e) {
        var p = clickLocation(e);
        var c = step === 1 ? polyCornerClicked(p) : lineCornerClicked(p);
        if (c >= 0) {
            draggingCorner = c;
            mouseDrag = true;
        } else {
            mouseDrag = false;
        }
    };

    $(canvas)[0].onmouseup = function () {
        mouseDrag = false;
    };

    $(canvas)[0].onmousemove = function (e) {
        if (mouseDrag) {
            var p = clickLocation(e);
            step === 1 ? poly[draggingCorner] = p : line[draggingCorner] = p;
        }
    };

    function drawPoly() {
        var i;
//        c2.clearRect(0, 0, w, h);
        c2.drawImage(img, 0, 0, w, h);
        c2.strokeStyle = '#f00';
        c2.lineWidth = 2;
        c2.beginPath();
        c2.moveTo(poly[0].x, poly[0].y);
        for (i = 1; i < 4; i++) {
            c2.lineTo(poly[i].x, poly[i].y);
        }
        c2.closePath();
        c2.stroke();

        for (i = 0; i < 4; i++) {
            c2.beginPath();
            c2.arc(poly[i].x, poly[i].y, 5, 0, 2 * Math.PI);
            c2.stroke();
        }
    }

    function drawLine() {
        c2.drawImage(transformedImg, 0, 0, w, h);

        c2.strokeStyle = '#f00';
        c2.lineWidth = 2;

        c2.beginPath();
        c2.moveTo(line[0].x, line[0].y);
        c2.lineTo(line[1].x, line[1].y);
        c2.closePath();
        c2.stroke();

        for (i = 0; i < 2; i++) {
            c2.beginPath();
            c2.arc(line[i].x, line[i].y, 5, 0, 2 * Math.PI);
            c2.stroke();
        }
    }

    function distance(a, b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    function clickLocation(e) {
        var x = e.pageX;
        var y = e.pageY;
        x -= canvas.offsetLeft;
        y -= canvas.offsetTop;
        return {x: x, y: y};
    }

    function polyCornerClicked(p) {
        var i;
        for (i = 0; i < 4; i++) {
            if (distance(p, poly[i]) < 5) {
                return i;
            }
        }
        return -1;
    }

    function lineCornerClicked(p) {
        if (distance(p, line[0]) < 5) {
            return 0;
        }
        if (distance(p, line[1]) < 5) {
            return 1;
        }
        return -1;
    }
}
