var currentUser = null;

function initUserControl() {

    function adminLoginAction() {
        $("#addAccountButton").removeClass("hidden");
        $("#saveViewButton").removeClass("hidden");
        $("#addCameraContextButton").removeClass("hidden");

        currentUser = "admin";
    }

    function userLoginAction(username) {
        $("#markStreetContextButton").removeClass("hidden");
        $("#justMyChangesButton").removeClass("hidden");

        currentUser = username;
    }

    function logoutAction() {
        $("#addAccountButton").addClass("hidden");
        $("#saveViewButton").addClass("hidden");
        $("#addCameraContextButton").addClass("hidden");
        $("#markStreetContextButton").addClass("hidden");
        $("#justMyChangesButton").addClass("hidden");

        cameras.forEach(function (c) {
            c.closeInfoW();
        });
        currentUser = null;
    }

    $("#loginButton").click(function () {
        var username = $("#usernameInput").val();
        var password = $("#passwordInput").val();

        if (username === "") {
            $("#loginDialogError").html("<br>Niste uneli korisnicko ime");
            return false;
        }
        if (password === "") {
            $("#loginDialogError").html("<br>Niste uneli lozinku");
            return false;
        }
        var p = {username: username, password: password};

        $.post("LoginServlet", $.param(p), function (user) {
            console.log(user);
            if (JSON.stringify(user) !== "{}") {
                if (username === "admin") {
                    adminLoginAction();
                } else {
                    userLoginAction(username);
                }
                $("#showLoginButton").addClass("hidden");
                $("#logoutButton").removeClass("hidden");
                $("#markStreetContextButton").removeClass("hidden");
                $("#loginModal").modal("hide");
                $("#logoutButton").html(user.name + " | <b>Odjavi se</b>");
            } else {
                $("#loginDialogError").html("<br>Nevalidni podaci");
            }
        });
    });

    $("#logoutButton").click(function () {
        $("#usernameInput").val("");
        $("#passwordInput").val("");
        $("#loginDialogError").html("");
        $.post("LogoutServlet");

        logoutAction();

        $("#showLoginButton").removeClass("hidden");
        $("#logoutButton").addClass("hidden");
    });

    $("#dodajNalogButton").click(function () {
        var ime = $("#newNameInput").val();
        var username = $("#newUsernameInput").val();
        var password = $("#newPasswordInput").val();

        if (ime === "") {
            $("#dodajNalogDialogError").html("<br>Niste uneli ime");
            return false;
        }

        if (username === "") {
            $("#dodajNalogDialogError").html("<br>Niste uneli korisnicko ime");
            return false;
        }
        if (password === "") {
            $("#dodajNalogDialogError").html("<br>Niste uneli lozinku");
            return false;
        }

        var p = {name: ime, username: username, password: password};

        $.post("AddAccountServlet", $.param({user: JSON.stringify(p)}), function (responseText) {
            if (responseText === "ok") {
                $("#dodajNalogModal").modal("hide");
            } else {
                $("#dodajNalogDialogError").html("<br>" + responseText);
            }
            console.log(responseText);
        });
    });

    $("#justMyChangesButton").click(function () {
        var shouldHide = !$("#justMyChangesButton")[0].shouldHide;
        console.log("should hide = " + shouldHide);
        for (var i in streets) {
            var s = streets[i];
            if (s.owner !== currentUser && shouldHide) {
                s.polyLine.setVisible(false);
            } else {
                s.polyLine.setVisible(true);
            }
        }
        displayMobileData(!shouldHide);
        if (shouldHide) {
            $("#justMyChangesButton").text("Prikazi sve podatke");
        } else {
            $("#justMyChangesButton").text("Prikazi samo moje podatke");
        }
        $("#justMyChangesButton")[0].shouldHide = shouldHide;
    });
}

