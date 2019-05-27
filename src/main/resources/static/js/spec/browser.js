function queryList() {
    var ip = $("#ip").val();
    var port = $("#port").val();
    var user = $("#user").val();
    var password = $("#password").val();
    var path = $("#path").val();

    $.ajax({
        url: '/ftp/list',
        type: 'POST',
        dataType: 'json',
        data: {
            "ip": ip,
            "port": port,
            "user": user,
            "password": password,
            "path": path
        },
        success: function (data) {
            $("#currentPath").val(data.path);
            $.each(data.children, function(i, file) {
                alert(file.name)
            })
        }
    })

}