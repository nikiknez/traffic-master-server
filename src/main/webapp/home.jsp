<!DOCTYPE html>
<html>
    <head>
        <title>Master Pocetna</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <link rel="stylesheet" href="css/styles.css"/>
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link rel="stylesheet" href="css/bootstrap-datetimepicker.min.css">
        <script src="js/jq/jquery.min.js"></script>
        <script src="js/jq/jquery.form.min.js"></script>
        <script src="js/bs/bootstrap.min.js"></script>
        <script src="js/bs/moment.js"></script>
        <script src="js/bs/sr.js"></script>
        <script src="js/bs/bootstrap-datetimepicker.min.js"></script>
    </head>

    <body>
        <div id="map"></div>

        <!--Pogled dialog-->
        <div class="modal" id="dodajPogledModal" >
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Unesi naziv lokacije</h4>
                    </div>
                    <div class="modal-body">
                        <input  type="text" class="form-control" id="nazivPogledaInput">
                        <strong style="color: red" id="pogledDialogError" class="hidden">
                            <br>Niste uneli naziv lokacije
                        </strong>
                    </div>
                    <div class="modal-footer">
                        <button id="sacuvajPogledButton" class="btn btn-success" data-dismiss="modal">Sacuvaj</button>
                        <button class="btn btn-danger" data-dismiss="modal">Ponisti</button>
                    </div>
                </div>
            </div>
        </div>

        <!--Kamere dialog-->
        <div class="modal" id="addCameraModal" >
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Informacije o kameri</h4>
                    </div>
                    <div class="modal-body">
                        <label for="nazivKamereInput">Naziv kamere:</label>
                        <input type="text" class="form-control" id="nazivKamereInput"
                               value="Autokomanda">
                        <br/>
                        <b>Tip izvora video snimka:</b>
                        <label class="radio-inline"><input type="radio" value="ip"
                                                           checked="true" name="videoTip">Internet kamera</label>
                        <label class="radio-inline"><input type="radio" value="file" name="videoTip">Video fajl</label>
                        <br/>
                        <div id="ipCamDiv">
                            <br/>
                            <label for="ipCamInput">Internet adresa kamere:</label>
                            <input type="text" class="form-control" id="ipCamInput"
                                   value="http://109.206.96.249:8080/cam_3.jpg">

                        </div>
                        <div id="fileDiv">
                            <br/>
                            <form id="uploadForm" action="UploadVideoServlet" method="post" enctype="multipart/form-data">
                                <label class="btn btn-default btn-file">
                                    <span id="fileNameText">Izaberi fajl</span> 
                                    <input id="fileInput" type="file" name="file" 
                                           accept="video/mp4" multiple="false" style="display: none;">
                                </label>
                                <input id="uploadFileButton" type="submit" 
                                       class="btn btn-primary hidden" value="Posalji fajl">
                            </form>
                            <div id="uploadingStatusDiv" style="margin-top: 5px" class="hidden">
                                <div class="loader"></div>
                                <span id="uploadingProgressInfo"></span>
                            </div>
                        </div>
                        <strong style="color: red" id="kameraDialogError">
                        </strong>
                    </div>
                    <div class="modal-footer">
                        <button id="sacuvajKameruButton" class="btn btn-success" data-dismiss="modal">Sacuvaj</button>
                        <button class="btn btn-danger" data-dismiss="modal">Ponisti</button>
                    </div>
                </div>
            </div>
        </div>

        <!--Login dialog-->
        <div class="modal" id="loginModal" >
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Prijava</h4>
                    </div>
                    <div class="modal-body">
                        <label for="usernameInput">Korisnicko ime:</label>
                        <input type="text" class="form-control" id="usernameInput">
                        <label for="passwordInput">Lozinka:</label>
                        <input type="password" class="form-control" id="passwordInput">
                        <strong style="color: red" id="loginDialogError">
                        </strong>
                    </div>
                    <div class="modal-footer">
                        <button id="loginButton" class="btn btn-success">Prijavi se</button>
                        <button class="btn btn-danger" data-dismiss="modal">Odustani</button>
                    </div>
                </div>
            </div>
        </div>

        <!--Dodaj nalog dialog-->
        <div class="modal" id="dodajNalogModal" >
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Novi nalog</h4>
                    </div>
                    <div class="modal-body">
                        <label for="newNameInput">Ime:</label>
                        <input type="text" class="form-control" id="newNameInput">
                        <label for="newUsernameInput">Korisnicko ime:</label>
                        <input type="text" class="form-control" id="newUsernameInput">
                        <label for="newPasswordInput">Lozinka:</label>
                        <input type="password" class="form-control" id="newPasswordInput">

                        <div class="checkbox">
                            <label><input type="checkbox" id="canAddCamera">Moze da doda kameru</label>
                        </div>
                        <div class="checkbox">
                            <label><input type="checkbox" id="canAddMark">Moze da doda oznaku</label>
                        </div>
                        <div class="checkbox">
                            <label><input type="checkbox" id="canAddStreet">Moze da oznaci ulicu</label>
                        </div> 

                        <strong style="color: red" id="dodajNalogDialogError">
                        </strong>
                    </div>
                    <div class="modal-footer">
                        <button id="dodajNalogButton" class="btn btn-success">Kreiraj nalog</button>
                        <button class="btn btn-danger" data-dismiss="modal">Odustani</button>
                    </div>
                </div>
            </div>
        </div>

        <!--Street info dialog-->
        <div class="modal" id="streetInfoModal" >
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Informacije</h4>
                    </div>
                    <div class="modal-body">
                        <label for="streetInfoText">Obavestenje:</label>
                        <textarea class="form-control" rows="3" id="streetInfoText"></textarea>
                        <br>
                        <label for="datetimepicker1">Vazi od:</label>
                        <div class='input-group date' id='datetimepicker1'>
                            <input type='text' class="form-control" id="streetInfoValidFrom"
                                   onkeydown="return false" onkeyup="return false" onkeypress="return false" />
                            <span class="input-group-addon">
                                <span class="glyphicon glyphicon-calendar"></span>
                            </span>
                            <script type="text/javascript">
                                $(function () {
                                    $('#datetimepicker1').datetimepicker();
                                });
                            </script>
                        </div>
                        <br>
                        <label for="datetimepicker2">Vazi do:</label>
                        <div class='input-group date' id='datetimepicker2'>
                            <input type='text' class="form-control" id="streetInfoValidTo"
                                   onkeydown="return false" onkeyup="return false" onkeypress="return false" />
                            <span class="input-group-addon">
                                <span class="glyphicon glyphicon-calendar"></span>
                            </span>
                            <script type="text/javascript">
                                $(function () {
                                    $('#datetimepicker2').datetimepicker();
                                });
                            </script>
                        </div>
                        <strong style="color: red" id="streetDialogError"></strong>
                    </div>
                    <div class="modal-footer">
                        <button id="saveStreetInfoButton" class="btn btn-success" data-dismiss="modal">Sacuvaj</button>
                        <button class="btn btn-danger" data-dismiss="modal">Ponisti</button>
                    </div>
                </div>
            </div>
        </div>

        <!--Login control-->
        <div id="userControlDiv">
            <button id="justMyChangesButton" class="btn btn-default hidden">Prikazi samo moje podatke</button>
            <button id="addAccountButton" class="btn btn-default hidden" data-toggle="modal" 
                    data-target="#dodajNalogModal">Dodaj nalog</button>
            <button id="showLoginButton" class="btn btn-default" data-toggle="modal" 
                    data-target="#loginModal">Prijavi se</button>
            <button id="logoutButton" class="btn btn-default hidden">Odjavi se</button>
        </div>

        <!--Center control-->
        <div id="centerControlDiv">
            <button id="saveViewButton" class="btn btn-default hidden" data-toggle="modal" 
                    data-target="#dodajPogledModal">Sacuvaj ovaj pogled</button>

            <span class="dropdown">
                <button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
                    Centriraj mapu <span class="caret"></span></button>
                <ul id="centersList" class="dropdown-menu">
                </ul>
            </span>
        </div>

        <!--Context Menu-->
        <div id="contextMenu" class="btn-group-vertical hidden contextMenu">
            <button id="addCameraContextButton" class="btn btn-default hidden"
                    data-toggle="modal" data-target="#addCameraModal">Dodaj kameru ovde</button>
            <button id="markStreetContextButton" class="btn btn-default hidden">Oznaci ulicu</button>
            <button id="addMarkContextButton" class="btn btn-default hidden"
                    data-toggle="modal" data-target="#streetInfoModal">Dodaj oznaku</button>
        </div>

        <!--Street Context Menu-->
        <div id="streetContextMenu" class="btn-group-vertical hidden contextMenu">
            <button id="bindToCamButton" class="btn btn-default hidden">Pridruzi kameri</button>
            <button id="streetInfoButton" class="btn btn-default hidden">Unesi informacije</button>
        </div>

        <!--Street drawing mod-->
        <div id="streetControlDiv" class="btn-group-vertical hidden">
            <button id="saveStreetButton" class="btn btn-success" 
                    data-toggle="modal" data-target="">Sacuvaj unos</button>
            <br/>
            <button id="cancleStreetButton" class="btn btn-danger">Ponisti unos</button>
        </div>

        <!--Camera select mode-->
        <div id="camSelectDiv" class="btn-group-vertical hidden">
            <button id="cancleCamSelectButton" class="btn btn-danger">Otkazi</button>
        </div>

        <!--Street select mode-->
        <div id="streetSelectDiv" class="btn-group-vertical hidden">
            <button id="cancleStreetSelectButton" class="btn btn-danger">Otkazi</button>
        </div>

        <div id="info1" class="alert alert-info alert-dismissable hidden">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">×</a>
            <strong>Uputstvo: </strong>
            <p>Levi klik za dodavanje nove tacke</p>
            <p>Desni za brisanje odabrane tacke</p>
        </div>

        <!-- Camera view configuration -->
        <div class="overlay hidden" id="camConfigOverlay">
            <canvas id="camConfigCanvas">
            </canvas>
            <div id="info2" class="alert alert-info alert-dismissable">
                <a href="#" class="close" data-dismiss="alert" aria-label="close">×</a>
                <strong>Uputstvo: </strong>
                <p>Pomeraj uglove poligona tako da se formira pravougaonik
                    nad delom ulice u okviru kog ce se pratiti kretanje</p>
            </div>
            <div id="info3" class="alert alert-info alert-dismissable hidden">
                <a href="#" class="close" data-dismiss="alert" aria-label="close">×</a>
                <strong>Uputstvo: </strong>
                <p>Pomeraj liniju tako da se poklopi sa objektom na slici
                    za koji znas koliko je dugacak u stvarnom svetu</p>
            </div>
            <div id="camConfigControlDiv" class="btn-group-vertical">
                <input type='text' class="form-control hidden" placeholder="Duzina linije [m]" id="lineLengthInput"/>
                <br/>
                <button id="saveCamConfigButton" class="btn btn-success hidden">Sacuvaj unos</button>
                <br/>
                <button id="prevCamConfigButton" class="btn btn-success hidden">Prethodni korak</button>
                <br/>
                <button id="nextCamConfigButton" class="btn btn-success">Sledeci korak</button>
                <br/>
                <button id="cancleCamConfigButton" class="btn btn-danger">Ponisti unos</button>
            </div>
        </div>

        <div id="info4" class="alert alert-info hidden">
            <strong>Uputstvo: </strong>
            <p>Levi klik na kameru koja posmatra ovu ulicu</p>
        </div>

        <div id="info5" class="alert alert-info hidden">
            <strong>Uputstvo: </strong>
            <p>Klikni na ulicu koju zelis da konfigurises</p>
        </div>

        <script
            src="https://maps.googleapis.com/maps/api/js?language=sr&key=AIzaSyCT5XMY1YeBcvRNHpje04pmrgQ0WeKzCso">
        </script>

        <script>
            var config = ${config};
        </script>

        <script src="js/util.js"></script>
        <script src="js/camera.js"></script>
        <script src="js/camConfig.js"></script>
        <script src="js/cameraSetup.js"></script>
        <script src="js/markedStreet.js"></script>
        <script src="js/streetDraw.js"></script>
        <script src="js/centerControl.js"></script>
        <script src="js/userControl.js"></script>
        <script src="js/main.js"></script>


    </body>
</html>