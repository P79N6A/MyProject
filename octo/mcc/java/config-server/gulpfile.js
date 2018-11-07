/*jshint node:true */
'use strict';
var gulp = require('gulp');
var $ = require('gulp-load-plugins')();
var del = require('del');
var browserify = require('browserify');
var transform = require('vinyl-transform');


var srcDir = 'app/';
var destDir = 'src/main/webapp/static/';

gulp.task('styles', function () {
    return gulp.src(srcDir + 'css/*.css')
        .pipe($.sourcemaps.init())
        .pipe($.postcss([
            require('autoprefixer-core')({browsers: ['last 1 version']})
        ]))
        .pipe($.minifyCss())
        .pipe($.sourcemaps.write('./'))
        .pipe(gulp.dest(destDir + 'css'));
});

gulp.task('scripts', function(){
    var browserified = transform(function (filename) {
        var b = browserify(filename);
        console.log(filename);
        return b.bundle();
    });
    browserified.on('error', function(e){
        console.error(e.message);
        this.emit('end');
    });
    return gulp.src(srcDir + 'js/**/*.js')
        .pipe(browserified)
        .pipe(gulp.dest(destDir + 'js'));
});

gulp.task('images', function () {
    return gulp.src(srcDir + 'img/**/*')
        .pipe($.imagemin({
            progressive: true,
            interlaced: true,
            // don't remove IDs from SVGs, they are often used
            // as hooks for embedding and styling
            svgoPlugins: [{cleanupIDs: false}]
        }))
        .pipe(gulp.dest(destDir + 'img'));
});

gulp.task('extras', function () {
    return gulp.src([
        srcDir + '*.*'
    ], {
        dot: true
    }).pipe(gulp.dest(destDir));
});

gulp.task('clean', function(cb){
    del([destDir + 'js/**/*', '!' + destDir + '/bower_components/**' ], function () {
        cb();
    });
});

gulp.task('watch', ['build'], function(){
    gulp.watch(srcDir + 'css/**/*.css', ['styles']);
    gulp.watch(srcDir + 'js/**/*.js', ['scripts']);
    gulp.watch(srcDir + 'images/**/*', ['images']);
    gulp.watch(srcDir + '*.*', ['extras']);
});

gulp.task('build', ['extras', 'styles', 'scripts', 'images'], function () {
    return gulp.src(destDir + '**/*').pipe($.size({title: 'build', gzip: true}));
});

gulp.task('default', ['clean'], function () {
    gulp.start('build');
});
