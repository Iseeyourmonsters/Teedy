'use strict';

/**
 * Register controller.
 */
angular.module('docs').controller('StatsModalCtrl', function ($scope, $uibModalInstance, Restangular, $translate, $dialog) {
    let typeChart, userChart;

    $scope.loadChart = function () {
        console.log("Trigger load chart")
        Restangular.one('auditlog/uploadStats').get().then(function (data) {
            console.log('Load Type chart:', data);
            $scope.initTypeChart(data);
        });

        Restangular.one('auditlog/userUploadCount').get().then(function (data) {
            console.log('Load User chart:', data);
            $scope.initUserChart(data);
        });
    };

    // 初始化类型饼图
    $scope.initTypeChart = function (data) {
        const ctx = document.getElementById('typeChart').getContext('2d');
        const labels = data.map(item => item.type);
        const counts = data.map(item => item.count);

        typeChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'
                    ]
                }]
            }
        });
    };

    // 初始化用户饼图
    $scope.initUserChart = function (data) {
        const ctx = document.getElementById('userChart').getContext('2d');
        const labels = data.map(item => item.username);
        const counts = data.map(item => item.count);

        userChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'
                    ]
                }]
            }
        });
    };

    // Call the function loadChart first
    $scope.loadChart();

    // Close function
    $scope.close = function () {
        $uibModalInstance.dismiss();
        if (typeChart) typeChart.destroy();
        if (userChart) userChart.destroy();
    };

});