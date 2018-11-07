#ifndef CALC_H_
#define CALC_H_

class ITest {
 public:
  virtual ~ITest() {}
  virtual int Add(int a, int b) = 0;
  virtual int Desc(int a, int b) = 0;
};

#endif // CALC_H_
