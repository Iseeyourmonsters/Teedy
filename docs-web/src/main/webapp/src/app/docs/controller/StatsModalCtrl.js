'use strict';

/**
 * Register controller.
 */
angular.module('docs').controller('StatsModalCtrl', function ($scope, $uibModalInstance, Restangular, $translate, $dialog) {
    let typeChart, userChart, timelineChart, fileTypeChart, fileSizeChart;
    
    // 初始化统计数据
    $scope.totalUploads = 0;
    $scope.uniqueUsers = 0;
    $scope.totalActivities = 0;
    $scope.activeUsers = 0;

    // 格式化文件大小
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    $scope.loadChart = function () {
        // 加载上传统计
        Restangular.one('auditlog/uploadStats').get().then(function (data) {
            $scope.initTypeChart(data);
            // 计算总上传数和独立用户数
            $scope.totalUploads = data.reduce((sum, item) => sum + item.total_count, 0);
            $scope.uniqueUsers = data.reduce((sum, item) => sum + item.unique_users, 0);
        });

        // 加载用户统计
        Restangular.one('auditlog/userUploadCount').get().then(function (data) {
            $scope.initUserChart(data);
        });

        // 加载活动时间线
        Restangular.one('auditlog/activityTimeline').get().then(function (data) {
            $scope.initTimelineChart(data);
        });

        // 加载用户活动统计
        Restangular.one('auditlog/userActivityStats').get().then(function (data) {
            $scope.totalActivities = data.reduce((sum, item) => sum + item.total_activities, 0);
            $scope.activeUsers = data.length;
        });

        // 加载文件类型统计
        Restangular.one('auditlog/fileTypeStats').get().then(function (data) {
            $scope.initFileTypeCharts(data);
        });
    };

    // 初始化类型饼图
    $scope.initTypeChart = function (data) {
        // 过滤掉User和UserRegistration类型
        data = data.filter(item => item.type !== 'User' && item.type !== 'UserRegistration');
        const ctx = document.getElementById('typeChart').getContext('2d');
        const labels = data.map(item => item.type);
        const counts = data.map(item => item.total_count);

        if (typeChart) {
            typeChart.destroy();
        }

        typeChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
                        '#FF9F40', '#8AC249', '#EA526F', '#23B5D3', '#279AF1'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'right'
                    },
                    title: {
                        display: true,
                        text: $translate.instant('chart.by_type')
                    }
                }
            }
        });
    };

    // 初始化用户饼图
    $scope.initUserChart = function (data) {
        const ctx = document.getElementById('userChart').getContext('2d');
        const labels = data.map(item => item.username);
        const counts = data.map(item => item.count);

        if (userChart) {
            userChart.destroy();
        }

        userChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
                        '#FF9F40', '#8AC249', '#EA526F', '#23B5D3', '#279AF1'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'right'
                    },
                    title: {
                        display: true,
                        text: $translate.instant('chart.by_user')
                    }
                }
            }
        });
    };

    // 初始化时间线图表
    $scope.initTimelineChart = function (data) {
        const ctx = document.getElementById('timelineChart').getContext('2d');
        
        // 处理数据
        const activities = data.map(item => ({
            x: new Date(item.timestamp),
            y: item.username,
            type: item.activity_type,
            entity: item.entity_type
        }));

        if (timelineChart) {
            timelineChart.destroy();
        }

        timelineChart = new Chart(ctx, {
            type: 'scatter',
            data: {
                datasets: [{
                    label: 'Activities',
                    data: activities,
                    backgroundColor: function(context) {
                        const type = context.raw.type;
                        switch(type) {
                            case 'CREATE': return '#4BC0C0';
                            case 'UPDATE': return '#FFCE56';
                            case 'DELETE': return '#FF6384';
                            default: return '#36A2EB';
                        }
                    }
                }]
            },
            options: {
                responsive: true,
                scales: {
                    x: {
                        type: 'time',
                        time: {
                            unit: 'day'
                        },
                        title: {
                            display: true,
                            text: $translate.instant('chart.time')
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: $translate.instant('chart.user')
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const item = context.raw;
                                return [
                                    $translate.instant('chart.user') + ': ' + item.y,
                                    $translate.instant('chart.type') + ': ' + item.type,
                                    $translate.instant('chart.entity') + ': ' + item.entity
                                ];
                            }
                        }
                    }
                }
            }
        });
    };

    // 初始化文件类型图表
    $scope.initFileTypeCharts = function (data) {
        // 文件类型数量统计
        const typeCtx = document.getElementById('fileTypeChart').getContext('2d');
        const labels = data.map(item => item.type);
        const counts = data.map(item => item.count);

        if (fileTypeChart) {
            fileTypeChart.destroy();
        }

        fileTypeChart = new Chart(typeCtx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
                        '#FF9F40', '#8AC249', '#EA526F', '#23B5D3', '#279AF1'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'right'
                    },
                    title: {
                        display: true,
                        text: $translate.instant('chart.by_file_type')
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = Math.round((value / total) * 100);
                                return `${label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });

        // 文件类型大小统计
        const sizeCtx = document.getElementById('fileSizeChart').getContext('2d');
        const sizes = data.map(item => item.total_size);

        if (fileSizeChart) {
            fileSizeChart.destroy();
        }

        fileSizeChart = new Chart(sizeCtx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: $translate.instant('chart.file_size'),
                    data: sizes,
                    backgroundColor: '#36A2EB'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    title: {
                        display: true,
                        text: $translate.instant('chart.by_file_size')
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return formatFileSize(context.raw);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            callback: function(value) {
                                return formatFileSize(value);
                            }
                        }
                    }
                }
            }
        });
    };

    // 初始化加载
    $scope.loadChart();

    // 关闭函数
    $scope.close = function () {
        $uibModalInstance.dismiss();
        if (typeChart) typeChart.destroy();
        if (userChart) userChart.destroy();
        if (timelineChart) timelineChart.destroy();
        if (fileTypeChart) fileTypeChart.destroy();
        if (fileSizeChart) fileSizeChart.destroy();
    };
});