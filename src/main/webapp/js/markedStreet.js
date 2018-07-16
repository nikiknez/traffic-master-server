var selectedStreet = null;
var camSelectMode = false;

function MarkedStreet(polyLine, options) {
    var self = this;
    self.polyLine = polyLine;

    if (options) {
        self.id = options.id;
        self.owner = options.owner;
        self.infoText = options.infoText;
        self.validFrom = options.validFrom;
        self.ValidTo = options.ValidTo;
    }

    polyLine.addListener('rightclick', function (e) {
        if (!camSelectMode && !drawingMode && !streetSelectMode && !self.camId) {
            selectedStreet = self;
            openStreetContextMenu(e.Ha);
        }
    });

    var iw = new google.maps.InfoWindow();
    iw.setOptions({maxWidth: 300});

    google.maps.event.addListener(self.polyLine, 'click', function (e) {
        if (streetSelectMode) {
            if (selectedCam.id === self.camId) {
                exitStreetSelectMode(self);
            } else {
                // error
            }
        }
        if (self.infoText) {
            iw.setContent(self.infoText);
            iw.setPosition(e.latLng);
            iw.open(map);
        }
    });

    self.setStreetData = function (data) {
        console.log("street " + self.id + " got data: " + data);
        self.data = data;
        self.polyLine.setOptions({strokeColor: intensityToColorMap(data.intensity)});
    };
}

function intensityToColorMap(intensity) {
    if (intensity > 10) {
        return 'green';
    }
    if (intensity > 5) {
        return 'yellow';
    }
    return 'red';
}

$("#bindToCamButton").click(function () {
    enterCamSelectMode();
});

$("#streetInfoButton").click(function () {
    $("#streetInfoText").val(selectedStreet.infoText);
    $("#streetInfoModal").modal("show");
});

$('#streetInfoModal').on('hidden.bs.modal', function () {
    selectedStreet = null;
});

$("#cancleCamSelectButton").click(function () {
    exitCamSelectMode();
});

$("#saveStreetInfoButton").click(function () {
    var text = $("#streetInfoText").val();
    if (text === "") {
        $("#streetDialogError").html("Niste uneli obavestenje");
        return false;
    }
    var from = $("#streetInfoValidFrom").val();
    var to = $("#streetInfoValidTo").val();

    var p = {id: selectedStreet.id, info: text, from: from, to: to};
    $.post("AddStreetInfoServlet", $.param(p), function (responseText) {
        if (responseText === "ok") {

        }
        console.log(responseText);
    });

    selectedStreet.infoText = text;
    selectedStreet = null;
});

function openStreetContextMenu(event) {
    $(".contextMenu").addClass("hidden");
    $("#bindToCamButton").addClass("hidden");
    if (!selectedStreet.camId && selectedStreet.owner === currentUser.username && currentUser.canAddCamera) {
        $("#bindToCamButton").removeClass("hidden");
    }
    $("#streetInfoButton").addClass("hidden");
    if (!selectedStreet.camId && selectedStreet.owner === currentUser.username && currentUser.canAddStreet) {
        $("#streetInfoButton").removeClass("hidden");
        if (selectedStreet.infoText) {
            $("#streetInfoButton").text("Izmeni informacije");
        } else {
            $("#streetInfoButton").text("Unesi informacije");
        }
    }

    var cMenu = $("#streetContextMenu");
    cMenu.removeClass("hidden");

    var cMenuW = cMenu.width();
    var cMenuH = cMenu.height();
    var bodyW = $(document.body).width();
    var bodyH = $(document.body).height();

    if (event.x + cMenuW < bodyW) {
        cMenu.css("left", event.x);
    } else {
        cMenu.css("left", bodyW - cMenuW);
    }

    if (event.y + cMenuH < bodyH) {
        cMenu.css("top", event.y);
    } else {
        cMenu.css("top", bodyH - cMenuH);
    }
}

function enterCamSelectMode() {
    camSelectMode = true;
    map.setOptions({disableDefaultUI: true});
    $("#centerControlDiv").hide();
    $("#userControlDiv").hide();
    $("#camSelectDiv").removeClass("hidden");
    $("#info4").removeClass("hidden");
}
function exitCamSelectMode(cam) {
    camSelectMode = false;
    if (cam) {
        selectedStreet.camId = cam.id;
    }
    selectedStreet = null;
    map.setOptions({disableDefaultUI: false});

    $("#centerControlDiv").show();
    $("#userControlDiv").show();
    $("#camSelectDiv").addClass("hidden");
    $("#info4").addClass("hidden");
}