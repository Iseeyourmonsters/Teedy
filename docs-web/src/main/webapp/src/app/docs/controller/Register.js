'use strict';

/**
 * Register controller.
 */
angular.module('docs').controller('Register', function ($scope, $uibModalInstance, Restangular, $translate, $dialog) {
    $scope.username = '';
    $scope.password = '';
    $scope.confirmPassword = '';
    $scope.email = '';

    // Close function
    $scope.close = function (username) {
        $uibModalInstance.close(username);
    };

    // Register function
    $scope.register = function (password, confirmPassword, username, email) {
        console.log(password, confirmPassword, username, email);
        if (password !== confirmPassword) {
            var title = $translate.instant('register.error_title');
            var msg = $translate.instant('register.error_password_not_match');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
            return;
        }

        // Invoke the REST API to register the user
        console.log('Registering user:', username);
        Restangular.one('userRegistration').put({
                username: username,
                password: password,
                email: email
            }, '', {}, {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        ).then(function () {
            var title = $translate.instant('register.create_success_title');
            var msg = $translate.instant('register.create_success_message', { username: username });
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
            $uibModalInstance.close(username);
        }).catch(function (error) {
            console.error('Registration failed:', error);
            var title = $translate.instant('register.error_title');
            var msg = $translate.instant('register.error_message');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
        });
    };

});