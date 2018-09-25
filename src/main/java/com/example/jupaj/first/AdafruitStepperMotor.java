package com.example.jupaj.first;

import android.util.Log;

//Comenzamos nombrando la clase que en este caso se llama AdafrutiStepperMotor
public class AdafruitStepperMotor {

    // Curva Senoidal, no lineal de 16
    // private static int[] __MICROSTEPCURVE = {0, 25, 50, 74, 98, 120, 141, 162, 180, 197, 212, 225, 236, 244, 250, 253, 255};
    //Primero se nombra un arreglo de enteros cuyo nombre es MICROSTEP_CURVE, el número de pasos o steps esta dado por la variable entera MICROSTEP.

    private static int[] MICROSTEP_CURVE = {0, 50, 98, 142, 180, 212, 236, 250, 255};
    private int MICROSTEPS = 8;         // 8 or 16
    private AdafruitMotorHat MC;
    // La variable revsteps representan los pasos en sentido contrario que deben de ser iguales a los que son directos, mientras que la variable doble Sec_per_Step son los segundos de intervalo entre cada paso del motor
    private int revsteps;
    private double sec_per_step;
    //La variable entero currentstep es el paso el cual esta dando en este momento, la variable boleana sleepBetweenSteps, es la encargada de decir si es que debe de haber espacio entre los pasos, mientras que la variable boleana alreadySet PWM habla acerca de si el motor esta encendido o no
    private int currentstep;
    private boolean sleepBetweenSteps = true;
    private boolean alreadySetPWM = false;
// La variable PWMA y PWMB son los puertos para poder enlazar o encender en la tarjeta Adafruit el motor, mientras que los AIN y BIN [1,2], son las variables con las cuales nos comunicamos para darle los pasos a la tarjeta y que esta a su vez los transmita al motor

    private int PWMA = 8;
    private int AIN2 = 9;
    private int AIN1 = 10;
    private int PWMB = 13;
    private int BIN2 = 12;
    private int BIN1 = 11;
// Se procede a nombrar una clase AdafruitStepper Motor la cual le daremos diferentes parámetros, AdafruitMotorHAT MC, el entero con el número de motor (int motorNomber), el entero con el número de pasos (int steps), y el boleano de SleepBetweenSteps).

    public AdafruitStepperMotor(AdafruitMotorHat MC, int motorNumber, int steps, boolean sleepBetweenSteps) {
        //El método this se usa para renombrar o dar valores que sólo serán usados dentro del método de esta manera se evitan conflictos de nombre

        this.MC = MC;
        this.revsteps = steps;
        this.sec_per_step = 0.01;
        this.currentstep = 0;
        this.sleepBetweenSteps = sleepBetweenSteps;

        // Esta primera parte es para decidir que motor es el que se dará entrada y poder llamar a sus diferentes partes.
        motorNumber -= 1;

        if (motorNumber == 0) {

            this.PWMA = 8;
            this.AIN2 = 9;
            this.AIN1 = 10;
            this.PWMB = 13;
            this.BIN2 = 12;
            this.BIN1 = 11;

        } else if (motorNumber == 1) {

            this.PWMA = 2;
            this.AIN2 = 3;
            this.AIN1 = 4;
            this.PWMB = 7;
            this.BIN2 = 6;
            this.BIN1 = 5;
// En caso de que el motor que se llame no esté entre 1 y 2, en ese caso se muestra el mensaje de error de que debe de decir  1 o 2

        } else {
            Log.e(".", "MotorHAT Stepper must be between 1 and 2 inclusive");
        }
    }

    // Si no estableces una velocidad, entonces se asumes que quieres dar espacio entre los pasos ( Que es lo que afectas al establecer la velocidad) Así que el método sobrecargado es llamado con la variable sleepBetweenSteps = Verdadero
//
    public void setSpeed(int rpm) {
        setSpeed(rpm, true);
        //¿por qué llama así a sleepBetweenSteps, si no está en los parámetros?
    }
    // Creo que si pones, la velocidad por default le da el valor a sleepBetweenSteps como verdadero lo cual llama al siguiente método, que calcula los segundos por paso, con base a las rpm o revoluciones por minuto que les fueron introducidas al método
    public void setSpeed(int rpm, boolean sleepBetweenSteps) {
        this.sleepBetweenSteps = sleepBetweenSteps;
        sec_per_step = 60.0 / (revsteps * rpm);
    }

    // Se inicializa el método un paso con el cuál se introduce la dirección y el estilo del paso que se dará así como las variables pwm_a y b con valor de 255. [Imagino que es valor arbitario que se dio para poder ver un paso significativo]
    private int oneStep(int dir, int style) {

        int pwm_a = 255;
        int pwm_b = 255;

        // Se configura la forma en la que se enrollará el motor
        int coils[] = {0, 0, 0, 0};
        int step2coils[][] = {
                {1, 0, 0, 0},
                {1, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1},
                {1, 0, 0, 1}


        };
        // Primero vamos a determinar qué clase de método de espaciado esta
        // Primero veremos el estilo single, ¿Cómo llamamos al Style?
        if (style == AdafruitMotorHat.SINGLE) {
            //Primero vemos como va avanzando en los pasos, en el caso de que la división entre el paso corriente / el micro paso si el módulo que me retorna es mayor a 0 es decir no es una división exacta continua
// Se usa el if con modulo para identificar aquellos números que no son pares porque los números pares tienen modulo 0 con esta operación.
            if ((currentstep / (MICROSTEPS / 2)) % 2 > 0) {

                if (dir == AdafruitMotorHat.FORWARD) {

                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }

            } else {

                // Si el current step es par, el módulo es 0 y entra al else
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }
//si el estilo es doble entra a este método

        } else if (style == AdafruitMotorHat.DOUBLE) {

            //Log.d(".", "Double step");
//En el caso de que el paso actual sea par entonces el módulo es 0 y entonces se le suma pasos, en el caso contrario se le resta hasta que sea 0 y salga del bucle.

            if ((currentstep / (MICROSTEPS / 2)) % 2 == 0) {

                // We're at an even step, weird
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }
//En el caso de que ni sea 0 entonces se le suman los microsteps de manera íntegra si es que va adelante el motor de lo contrario se restan
            } else {

                // Go to next even step
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }
//En el caso de que el estilo sea Intervalo y va hacia adelante entonces se le suma la mitad de los Microsteps al paso corriente si va adelante o se le resta la mitad de los pasos en el caso de que no vaya adelante

        } else if (style == AdafruitMotorHat.INTERLEAVE) {

            if (dir == AdafruitMotorHat.FORWARD) {
                currentstep += (MICROSTEPS / 2);
            } else {
                currentstep -= (MICROSTEPS / 2);
            }
//En el caso de que el estilo sea microstep y vaya adelante se le va sumando un micro paso por micro paso en caso contrario se le resta de un micro paso en micro paso
        } else if (style == AdafruitMotorHat.MICROSTEP) {

            if (dir == AdafruitMotorHat.FORWARD) {
                currentstep++;
            } else {

                currentstep--;
                // Ir al siguiente paso y envolver ya que primero suma current step+4*Microstep y luego currentstep*=Currentstep* MOD MICROSTEP*4= Currentstep MOD Microstep*4+4*MIcrostep MOD MICROSTEP*4=CurrentStep MOD Microstep*4
                currentstep += (MICROSTEPS * 4);
                currentstep %= (MICROSTEPS * 4);
            }
// El siguiente paso es saber que los motores están apagados =0
            pwm_a = 0;
            pwm_b = 0;

// si el paso actual es mayor a 0 y el paso actual menor a los micro pasos programados, entonces el pwm_a es igual a será el valor de la curva cuyo índice sea Microstep-current step, nunca es 0 ya que microstep>current step en cambio para pwm_b toma directo el valor de la curva con el índice= current step
            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep];
//  en el caso de que el paso corriente sea mayor o igual a los micropasos y que aparte el paso corriente sea mejor al doble de los micro pasos (2da vuelta), entonces pwm_a toma el valor del índice de Currentstep-microstep lo cual es >=0, mientrasa que en el pwm_b>0 por las condiciones del condicional de entrada.
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 2 - currentstep];
// Si el paso corriente es mayor al doble o igual y menor a 3, entonces se comienza a repetir el patrón del anterior enunciado, esto para permitir que siempre caiga en los índices de la curva el valor.
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS * 3 - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep - MICROSTEPS * 2];
//Los enunciados se conjugan primero microstep- current step y luego al reves, de esta manera se da el torque para el motor
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS * 3];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 4 - currentstep];
            }
        }

        // Ir al siguiente paso y envolver, mismo enunciado que el de estilo microstep
        currentstep += (MICROSTEPS * 4);
        currentstep %= (MICROSTEPS * 4);

        // Sólo usado para micro pasos, en caso contrario siempre prendido
// Como son micro pasos, se usan arreglos de 1 en uno para un avance muy lento, contrario a los demás que se usan pasos grandes
        if (!alreadySetPWM || style == AdafruitMotorHat.MICROSTEP) {

            // Si no usamos micro stepping entonces el valor siempre será el mismo
            // Así que solo hay que usarlo una vez si no gastas 5 milisegundos de acuerdo a cada ingreso
            alreadySetPWM = true;
            MC.getPwm().setPWM(PWMA, 0, pwm_a * 16);
            MC.getPwm().setPWM(PWMB, 0, pwm_b * 16);
        }

        if (style == AdafruitMotorHat.MICROSTEP) {
            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                coils = new int[]{1, 1, 0, 0};
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                coils = new int[]{0, 1, 1, 0};
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                coils = new int[]{0, 0, 1, 1};
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                coils = new int[]{1, 0, 0, 1};
            }
        } else {
            coils = step2coils[currentstep / (MICROSTEPS / 2)];

        }

        //Log.d(".", "coils state = " + coils);

        MC.setPin(AIN2, coils[0]);
        MC.setPin(BIN1, coils[1]);
        MC.setPin(AIN1, coils[2]);
        MC.setPin(BIN2, coils[3]);

        return currentstep;
    }
// En este método se introducen como parámetros el número de pasos, la dirección y estilo de pasos, se usa los segundos por paso y el último paso es 0

    public void step(int steps, int direction, int stepstyle) {

        double s_per_s = sec_per_step;
        int lateststep = 0;

        if (stepstyle == AdafruitMotorHat.INTERLEAVE) {
            s_per_s /= 2.0;
        } else if (stepstyle == AdafruitMotorHat.MICROSTEP) {
            s_per_s /= MICROSTEPS;
            steps *= MICROSTEPS;
        }

        Log.d(".", "secs per step: " + s_per_s);

        for (int s = 1; s <= steps; s++) {

            lateststep = oneStep(direction, stepstyle);

            if (sleepBetweenSteps) {
                try {
                    Thread.sleep((long) s_per_s * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (stepstyle == AdafruitMotorHat.MICROSTEP) {


// Este es un caso extremo, si estamos entre pasos completos, solo sigamos
            // Así que terminamos en un paso completo
            while (lateststep != 0 && lateststep != MICROSTEPS) {

                lateststep = oneStep(direction, stepstyle);

                if (sleepBetweenSteps) {
                    try {
                        Thread.sleep((long) s_per_s * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

