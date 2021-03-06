var cameras = [];

function updateCamerasFromConfig() {
    var cams = [];
    for (var i in cameras) {
        cams[cameras[i].id] = cameras[i];
    }
    for (var i in config.cameras) {
        var c = config.cameras[i];

        var cam;
        if (!cams[c.id]) {
            if (c.type === "ip") {
                cam = new IpCamera(c.name, c.location, c.ipAddress);
            } else {
                cam = new FileCamera(c.name, c.location, c.videoFileName);
            }
            cam.id = c.id;
            cameras.push(cam);
        } else {
            cam = cams[c.id];
        }
        if (c.streets.length > cam.myStreets.length) {
            cam.myStreets = c.streets;
        }
        assignCamIdToStreets(cam.id, cam.myStreets);
    }
}
function assignCamIdToStreets(camId, streetIds) {
    for (var i in streetIds) {
        var sId = streetIds[i];
        if (streets[sId]) {
            streets[sId].camId = camId;
        }
    }
}

function initCameraSetup() {
    updateCamerasFromConfig();
    function tipSelect() {
        var tip = $("input[name=videoTip]:checked").val();
        if (tip === "ip") {
            $("#ipCamDiv").show();
            $("#fileDiv").hide();
        } else {
            $("#ipCamDiv").hide();
            $("#fileDiv").show();
        }
    }
    tipSelect();
    $("input[name=videoTip]").click(tipSelect);
    $("#uploadingStatusDiv").addClass("hidden");
    $("#fileNameText").text("Izaberi fajl");

    var fileNode = document.getElementById("fileInput");
    fileNode.onchange = function () {
        $("#fileNameText").text(this.files[0].name);
        $("#kameraDialogError").html("");
        $("#uploadFileButton").removeClass("hidden");
    };

    var file = null;
    $("#uploadForm").ajaxForm({
        beforeSend: function () {
            console.log("before send");
            $("#uploadingStatusDiv").removeClass("hidden");
            $(".loader").removeClass("hidden");
            $("#uploadingProgressInfo").text("");
            file = null;
        },
        success: function (msg) {
            console.log("success " + msg);
            file = msg;
            $("#uploadingProgressInfo").text("Video poslat");
        },
        error: function (msg) {
            console.log("error " + msg);
        },
        complete: function (msg) {
            console.log("complete ");
            console.log(msg);
            $(".loader").addClass("hidden");
            $("#uploadFileButton").addClass("hidden");
        },
        uploadProgress: function (event, position, total, percentComplete) {
            console.log("progress");
            console.log(arguments);
            $("#uploadingProgressInfo").text("Video se salje...[" + percentComplete + "%]");
        }
    });

    $("#sacuvajKameruButton").click(function () {
        var name = $("#nazivKamereInput").val();
        var ipAddress = $("#ipCamInput").val();
        var tip = $("input[name=videoTip]:checked").val();

        if (name === "") {
            $("#kameraDialogError").html("<br>Niste uneli naziv kamere");
            return false;
        }
        if (tip === "ip" && ipAddress === "") {
            $("#kameraDialogError").html("<br>Niste uneli internet adresu kamere");
            return false;
        }
        if (tip === "file" && !file) {
            $("#kameraDialogError").html("<br>Niste poslali video fajl");
            return false;
        }

        var p = {name: name, type: tip};
        p.location = JSON.stringify(lastClickLocation);
        if (tip === "ip") {
            p.ipAddress = ipAddress;
        }
        console.log(p);
        $.post("AddCameraServlet", $.param(p), function (c) {
            console.log(c);
            var cam;
            if (c.type === "ip") {
                cam = new IpCamera(c.name, c.location, c.ipAddress);
            } else if (c.type === "file") {
                cam = new FileCamera(c.name, c.location, c.videoFileName);
            }
            if (cam) {
                cam.id = c.id;
                cameras.push(cam);
            }
        });
    });

    $("#cancleStreetSelectButton").click(function () {
        exitStreetSelectMode();
    });

    $('#addCameraModal').on('hidden.bs.modal', function () {
        console.log("hiding camera modal");
        $("#uploadingStatusDiv").addClass("hidden");
        $("#uploadFileButton").addClass("hidden");
        file = null;
    });
}
